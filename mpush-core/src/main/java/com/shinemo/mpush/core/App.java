package com.shinemo.mpush.core;

import com.google.common.collect.Lists;
import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.core.server.GatewayServer;
import com.shinemo.mpush.tools.ConfigCenter;
import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.redis.RedisNode;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZkUtil;
import com.shinemo.mpush.tools.zk.manage.ServerManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by ohun on 2016/1/5.
 */
public final class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final App APP = new App();

    public static void main(String[] args) throws Exception {
        LOGGER.error("mpush app start begin....");
        APP.init();
        APP.startConnectionServer();
        APP.startGatewayServer();
        LOGGER.error("mpush app start end....");
    }

    private void init() throws IOException {
        ConfigCenter.INSTANCE.init();
        LOGGER.error("mpush app config center init success....");
    }

    public void startConnectionServer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final int port = ConfigCenter.INSTANCE.getConnectionServerPort();
                ConnectionServer server = new ConnectionServer(port);
                server.init();
                server.start(new Server.Listener() {
                    @Override
                    public void onSuccess() {
                        registerServerToZK(port, PathEnum.CONNECTION_SERVER);
                        LOGGER.error("mpush app start connection server success....");
                    }

                    @Override
                    public void onFailure(String message) {
                        LOGGER.error("mpush app start connection server failure, jvm exit with code -1");
                        System.exit(-1);
                    }
                });
            }
        });
        t.setDaemon(false);
        t.setName("conn-server-thread");
        t.start();
    }

    public void startGatewayServer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final int port = ConfigCenter.INSTANCE.getGatewayServerPort();
                GatewayServer server = new GatewayServer(port);
                server.init();
                server.start(new Server.Listener() {
                    @Override
                    public void onSuccess() {
                        registerServerToZK(port, PathEnum.GATEWAY_SERVER);
                        LOGGER.error("mpush app start gateway server success....");
                    }

                    @Override
                    public void onFailure(String message) {
                        System.exit(-2);
                        LOGGER.error("mpush app start gateway server failure, jvm exit with code -2");
                    }
                });
            }
        });
        t.setDaemon(false);
        t.setName("gateway-server-thread");
        t.start();
    }

    private void registerServerToZK(int port, PathEnum path) {
        String p = Integer.toString(port);
        ServerApp app = new ServerApp(InetAddressUtil.getInetAddress(), p);
        ServerManage manage = new ServerManage(app, path);
        manage.start();
        LOGGER.error("mpush app register server:{} to zk success", p);
    }

    public void startRedisClient() {
        RedisNode node1 = new RedisNode("10.1.20.74", 6379, "ShineMoIpo");

        RedisGroup group1 = new RedisGroup();
        group1.addRedisNode(node1);

        List<RedisGroup> groupList = Lists.newArrayList(group1);
        ZkUtil.instance.registerPersist(PathEnum.REDIS_SERVER.getPathByIp(InetAddressUtil.getInetAddress())
                , Jsons.toJson(groupList));
    }
}
