package com.mpush.test.connection.mpns;

import com.google.common.collect.Lists;
import com.mpush.common.AbstractClient;
import com.mpush.zk.ZKServerNode;

import java.util.List;

public class ConnectTestClient extends AbstractClient {

    private final List<ZKServerNode> applicationLists = Lists.newArrayList(
            new ZKServerNode("111.1.57.148", 20882, "111.1.57.148", ""));

    public List<ZKServerNode> getServers() {
        return Lists.newArrayList(applicationLists);
    }
}
