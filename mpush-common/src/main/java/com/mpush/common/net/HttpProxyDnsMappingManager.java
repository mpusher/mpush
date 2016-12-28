/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.common.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.api.spi.Spi;
import com.mpush.api.spi.common.ServiceDiscoveryFactory;
import com.mpush.api.spi.net.DnsMapping;
import com.mpush.api.spi.net.DnsMappingManager;
import com.mpush.api.srd.ServiceDiscovery;
import com.mpush.api.srd.ServiceListener;
import com.mpush.api.srd.ServiceNode;
import com.mpush.tools.Jsons;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.NamedPoolThreadFactory;
import com.mpush.tools.thread.ThreadNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mpush.api.srd.ServiceNames.DNS_MAPPING;
import static com.mpush.tools.Utils.checkHealth;

@Spi(order = 1)
public class HttpProxyDnsMappingManager extends BaseService implements DnsMappingManager, Runnable, ServiceListener {
    private final Logger logger = LoggerFactory.getLogger(HttpProxyDnsMappingManager.class);

    protected final Map<String, List<DnsMapping>> mappings = Maps.newConcurrentMap();

    private final Map<String, List<DnsMapping>> all = Maps.newConcurrentMap();
    private Map<String, List<DnsMapping>> available = Maps.newConcurrentMap();

    private ScheduledExecutorService executorService;

    @Override
    protected void doStart(Listener listener) throws Throwable {
        ServiceDiscovery discovery = ServiceDiscoveryFactory.create();
        discovery.subscribe(DNS_MAPPING, this);
        discovery.lookup(DNS_MAPPING).forEach(this::add);

        if (all.size() > 0) {
            executorService = Executors.newSingleThreadScheduledExecutor(
                    new NamedPoolThreadFactory(ThreadNames.T_HTTP_DNS_TIMER)
            );
            executorService.scheduleAtFixedRate(this, 1, 20, TimeUnit.SECONDS); //20秒 定时扫描dns
        }
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (executorService != null) {
            executorService.shutdown();
        }
        listener.onSuccess();
    }

    @Override
    public void init() {
        all.putAll(CC.mp.http.dns_mapping);
        available.putAll(CC.mp.http.dns_mapping);
    }

    @Override
    public boolean isRunning() {
        return executorService != null && !executorService.isShutdown();
    }

    public void update(Map<String, List<DnsMapping>> nowAvailable) {
        available = nowAvailable;
    }

    public Map<String, List<DnsMapping>> getAll() {
        return all;
    }

    public DnsMapping lookup(String origin) {
        List<DnsMapping> list = mappings.get(origin);

        if (list == null || list.isEmpty()) {
            if (available.isEmpty()) return null;
            list = available.get(origin);
        }

        if (list == null || list.isEmpty()) return null;
        int L = list.size();
        if (L == 1) return list.get(0);
        return list.get((int) (Math.random() * L % L));
    }

    @Override
    public void run() {
        logger.debug("do dns mapping checkHealth ...");
        Map<String, List<DnsMapping>> all = this.getAll();
        Map<String, List<DnsMapping>> available = Maps.newConcurrentMap();
        all.forEach((key, dnsMappings) -> {
            List<DnsMapping> okList = Lists.newArrayList();
            dnsMappings.forEach(dnsMapping -> {
                if (checkHealth(dnsMapping.getIp(), dnsMapping.getPort())) {
                    okList.add(dnsMapping);
                } else {
                    logger.warn("dns can not reachable:" + Jsons.toJson(dnsMapping));
                }
            });
            available.put(key, okList);
        });
        this.update(available);
    }

    @Override
    public void onServiceAdded(String path, ServiceNode node) {
        add(node);
    }

    @Override
    public void onServiceUpdated(String path, ServiceNode node) {
        add(node);
    }

    @Override
    public void onServiceRemoved(String path, ServiceNode node) {
        mappings.computeIfAbsent(node.getAttr("origin"), k -> new ArrayList<>())
                .remove(new DnsMapping(node.getHost(), node.getPort()));
    }

    private void add(ServiceNode node){
        mappings.computeIfAbsent(node.getAttr("origin"), k -> new ArrayList<>())
                .add(new DnsMapping(node.getHost(), node.getPort()));
    }
}
