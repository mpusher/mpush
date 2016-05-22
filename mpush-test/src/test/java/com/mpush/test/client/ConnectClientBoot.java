package com.mpush.test.client;

import com.google.common.collect.Lists;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.tools.log.Logs;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKServerNodeWatcher;
import com.mpush.zk.node.ZKServerNode;

import java.util.List;

public class ConnectClientBoot {
    private final ZKServerNodeWatcher listener = ZKServerNodeWatcher.buildConnect();

    public void start() {
        ZKClient.I.init();
        RedisManager.I.init();
        listener.beginWatch();
    }

    public List<ZKServerNode> getServers() {
        return Lists.newArrayList(listener.getCache().values());
    }
}