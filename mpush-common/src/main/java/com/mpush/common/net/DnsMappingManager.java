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
import com.mpush.tools.Jsons;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.data.DnsMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mpush.tools.Utils.checkHealth;

public class DnsMappingManager extends BaseService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(DnsMappingManager.class);

    public static final DnsMappingManager I = new DnsMappingManager();

    private DnsMappingManager() {
    }

    private final Map<String, List<DnsMapping>> all = Maps.newConcurrentMap();
    private Map<String, List<DnsMapping>> available = Maps.newConcurrentMap();

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void doStart(Listener listener) throws Throwable {
        scheduledExecutorService.scheduleAtFixedRate(this, 1, 20, TimeUnit.SECONDS); //20秒 定时扫描dns
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        scheduledExecutorService.shutdown();
    }


    @Override
    public void init() {
        logger.error("start init dnsMapping");
        all.putAll(CC.mp.http.dns_mapping);
        available.putAll(CC.mp.http.dns_mapping);
        logger.error("end init dnsMapping");
    }

    @Override
    public boolean isRunning() {
        return !scheduledExecutorService.isShutdown();
    }

    public void update(Map<String, List<DnsMapping>> nowAvailable) {
        available = nowAvailable;
    }

    public Map<String, List<DnsMapping>> getAll() {
        return all;
    }

    public DnsMapping lookup(String origin) {
        if (available.isEmpty())
            return null;
        List<DnsMapping> list = available.get(origin);
        if (list == null || list.isEmpty())
            return null;
        int L = list.size();
        if (L == 1)
            return list.get(0);
        return list.get((int) (Math.random() * L % L));
    }

    @Override
    public void run() {
        logger.debug("start dns mapping checkHealth");
        Map<String, List<DnsMapping>> all = I.getAll();
        Map<String, List<DnsMapping>> available = Maps.newConcurrentMap();
        for (Map.Entry<String, List<DnsMapping>> entry : all.entrySet()) {
            String key = entry.getKey();
            List<DnsMapping> value = entry.getValue();
            List<DnsMapping> nowValue = Lists.newArrayList();
            for (DnsMapping temp : value) {
                boolean isOk = checkHealth(temp.getIp(), temp.getPort());
                if (isOk) {
                    nowValue.add(temp);
                } else {
                    logger.error("dns can not reachable:" + Jsons.toJson(temp));
                }
            }
            available.put(key, nowValue);
        }
        I.update(available);
    }
}
