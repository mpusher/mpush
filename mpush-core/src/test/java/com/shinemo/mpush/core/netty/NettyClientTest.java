package com.shinemo.mpush.core.netty;


import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.Strings;
import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ohun on 2015/12/24.
 */
public class NettyClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientTest.class);

    public void setUp() throws Exception {
    }

    private List<ServerApp> getAllServers() {
        List<String> list = ZkUtil.instance.getChildrenKeys(ZKPath.CONNECTION_SERVER.getPath());
        if (list == null || list.isEmpty()) return Collections.EMPTY_LIST;
        List<ServerApp> servers = new ArrayList<>();
        for (String name : list) {
            String json = ZkUtil.instance.get(ZKPath.CONNECTION_SERVER.getFullPath(name));
            if (Strings.isBlank(json)) continue;
            ServerApp server = Jsons.fromJson(json, ServerApp.class);
            if (server != null) servers.add(server);
        }
        return servers;
    }

    public void testClient() throws Exception {
        List<ServerApp> serverApps = getAllServers();
        if (serverApps == null || serverApps.isEmpty()) return;
        int index = (int) ((Math.random() % serverApps.size()) * serverApps.size());
        ServerApp server = serverApps.get(index);
        ClientChannelHandler handler = new ClientChannelHandler();
        final Client client = NettyClientFactory.INSTANCE.createGet(server.getIp(), server.getPort(), handler);
        client.init();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                client.start();
            }
        });
        t.setDaemon(false);
        t.start();
    }

    public static void main(String[] args) throws Exception {
        NettyClientTest test = new NettyClientTest();
        test.setUp();
        test.testClient();
    }
}
