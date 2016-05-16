package com.mpush.test.connection.mpns;

import com.google.common.collect.Lists;
import com.mpush.common.AbstractClient;
import com.mpush.push.zk.listener.ConnectZKListener;
import com.mpush.zk.ZKServerNode;

import java.util.List;

public class ConnectTestClient extends AbstractClient {
    private final ConnectZKListener listener = new ConnectZKListener();

    public ConnectTestClient() {
        registerListener(listener);
    }

    public List<ZKServerNode> getServers() {
        return Lists.newArrayList(listener.getManager().getList());
    }
}
