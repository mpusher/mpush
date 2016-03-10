package com.shinemo.mpush.tools.dns.manage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.dns.DnsMapping;

public class DnsMappingManage {

    private static final Logger LOG = LoggerFactory.getLogger(DnsMappingManage.class);

    private DnsMappingManage() {
    }

    public static final DnsMappingManage holder = new DnsMappingManage();

    private Map<String, List<DnsMapping>> all = Maps.newConcurrentMap();
    private Map<String, List<DnsMapping>> available = Maps.newConcurrentMap();

    private Worker worker = new Worker();

    private ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

    public void init() {
        LOG.error("start init dnsMapping");
        all.putAll(ConfigCenter.holder.dnsMapping());
        available.putAll(ConfigCenter.holder.dnsMapping());
        pool.scheduleAtFixedRate(worker, 1, 20, TimeUnit.SECONDS); //20秒 定时扫描dns
        LOG.error("end init dnsMapping");
    }

    public void update(Map<String, List<DnsMapping>> nowAvailable) {
        available = nowAvailable;
    }

    public Map<String, List<DnsMapping>> getAll() {
        return all;
    }

    public DnsMapping translate(String origin) {
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

    public void shutdown() {
        pool.shutdown();
    }

    public static class Worker implements Runnable {

        private static final Logger log = LoggerFactory.getLogger(Worker.class);

        @Override
        public void run() {

            log.debug("start dns mapping telnet");

            Map<String, List<DnsMapping>> all = DnsMappingManage.holder.getAll();

            Map<String, List<DnsMapping>> available = Maps.newConcurrentMap();

            for (Map.Entry<String, List<DnsMapping>> entry : all.entrySet()) {
                String key = entry.getKey();
                List<DnsMapping> value = entry.getValue();
                List<DnsMapping> nowValue = Lists.newArrayList();
                for (DnsMapping temp : value) {
                    boolean canTelnet = MPushUtil.telnet(temp.getIp(), temp.getPort());
                    if (canTelnet) {
                        nowValue.add(temp);
                    } else {
                        log.error("dns can not reachable:" + Jsons.toJson(temp));
                    }
                }
                available.put(key, nowValue);
            }

            DnsMappingManage.holder.update(available);

        }
    }
}
