package com.shinemo.mpush.core.netty;


import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import com.shinemo.mpush.tools.ConfigCenter;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZkUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by ohun on 2015/12/24.
 */
public class NettyClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientTest.class);

    @Before
    public void setUp() throws Exception {
        ConfigCenter.INSTANCE.init();
    }


    @Test
    public void testClient() throws Exception {
        List<String> hosts = ZkUtil.instance.getChildrenKeys(PathEnum.CONNECTION_SERVER.getPath());
        if (hosts == null || hosts.isEmpty()) return;
        for (String name : hosts) {
            String json = ZkUtil.instance.get(PathEnum.CONNECTION_SERVER.getPathByName(name));
            ServerApp server = Jsons.fromJson(json, ServerApp.class);
            if (server == null) continue;
            ClientChannelHandler handler = new ClientChannelHandler();
            final Client client = NettyClientFactory.INSTANCE.get(server.getIp()
                    , Integer.parseInt(server.getPort()), handler);
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
    }

    public static void main(String[] args) throws Exception {
        NettyClientTest test = new NettyClientTest();
        test.setUp();
        test.testClient();
    }
}
