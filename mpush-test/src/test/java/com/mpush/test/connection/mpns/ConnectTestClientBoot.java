package com.mpush.test.connection.mpns;

import com.google.common.collect.Lists;
import com.mpush.zk.listener.ZKServerNodeWatcher;
import com.mpush.zk.node.ZKServerNode;

import java.util.List;

public class ConnectTestClientBoot {
    private final ZKServerNodeWatcher listener = ZKServerNodeWatcher.buildConnect();

    public void start() {
        listener.beginWatch();
    }

    public List<ZKServerNode> getServers() {
        return Lists.newArrayList(listener.getCache().values());
    }
}
