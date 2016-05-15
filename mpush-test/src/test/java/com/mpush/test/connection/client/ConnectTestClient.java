package com.mpush.test.connection.client;

import com.google.common.collect.Lists;
import com.mpush.common.AbstractClient;
import com.mpush.zk.ZKNodeManager;
import com.mpush.zk.ZKServerNode;
import com.mpush.push.zk.listener.ConnectZKListener;
import com.mpush.tools.spi.ServiceContainer;

import java.util.List;

public class ConnectTestClient extends AbstractClient {

    @SuppressWarnings("unchecked")
    private ZKNodeManager<ZKServerNode> connectionServerManage = ServiceContainer.load(ZKNodeManager.class);

    public ConnectTestClient() {
        registerListener(new ConnectZKListener());
    }

    public List<ZKServerNode> getServers() {
        return Lists.newArrayList(connectionServerManage.getList());
    }

}