package com.shinemo.mpush.core;

import com.google.common.collect.Lists;
import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.core.server.GatewayServer;
import com.shinemo.mpush.tools.ConfigCenter;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.redis.RedisNode;
import com.shinemo.mpush.tools.thread.ThreadPoolUtil;
import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZkUtil;
import com.shinemo.mpush.tools.zk.listener.impl.RedisPathListener;
import org.apache.zookeeper.data.Stat;
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
    private ConnectionServer connectionServer;
    private GatewayServer gatewayServer;

    public static void main(String[] args) throws Exception {
        LOGGER.error("mpush app start begin....");
        APP.init();
        APP.initRedisClient();
        APP.startConnectionServer();
        APP.startGatewayServer();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (APP.connectionServer != null) {
                    APP.connectionServer.stop(null);
                }
                if (APP.gatewayServer != null) {
                    APP.gatewayServer.stop(null);
                }
            }
        });
        LOGGER.error("mpush app start end....");
    }

    private void init() throws IOException {
        ConfigCenter.INSTANCE.init();
        LOGGER.error("mpush app config center init success....");
    }

    public void startConnectionServer() {
        ThreadPoolUtil.newThread(new Runnable() {
            @Override
            public void run() {
                final int port = ConfigCenter.INSTANCE.getConnectionServerPort();
                ConnectionServer server = new ConnectionServer(port);
                server.init();
                server.start(new Server.Listener() {
                    @Override
                    public void onSuccess() {
                        registerServerToZK(port, ZKPath.CONNECTION_SERVER);
                        LOGGER.error("mpush app start connection server success....");
                    }

                    @Override
                    public void onFailure(String message) {
                        LOGGER.error("mpush app start connection server failure, jvm exit with code -1");
                        System.exit(-1);
                    }
                });
                APP.connectionServer = server;
            }
        }, "conn-server", false).start();
    }

    public void startGatewayServer() {
        ThreadPoolUtil.newThread(new Runnable() {
            @Override
            public void run() {
                final int port = ConfigCenter.INSTANCE.getGatewayServerPort();
                GatewayServer server = new GatewayServer(port);
                server.init();
                server.start(new Server.Listener() {
                    @Override
                    public void onSuccess() {
                        registerServerToZK(port, ZKPath.GATEWAY_SERVER);
                        LOGGER.error("mpush app start gateway server success....");
                    }

                    @Override
                    public void onFailure(String message) {
                        System.exit(-2);
                        LOGGER.error("mpush app start gateway server failure, jvm exit with code -2");
                    }
                });
                APP.gatewayServer = server;
            }
        }, "gateway-server", false).start();
    }

    private void registerServerToZK(int port, ZKPath path) {
        ServerApp app = new ServerApp(MPushUtil.getLocalIp(), port);
        ZkUtil.instance.registerEphemeralSequential(path.getWatchPath(), Jsons.toJson(app));
        LOGGER.error("mpush app register server:{} to zk success", port);
    }

    public void initRedisClient() throws Exception {
        Stat stat = ZkUtil.instance.getClient().checkExists().forPath(ZKPath.REDIS_SERVER.getPath());
        if (stat == null) {
            RedisNode node1 = new RedisNode("10.1.20.74", 6379, "ShineMoIpo");
            RedisGroup group1 = new RedisGroup();
            group1.addRedisNode(node1);
            List<RedisGroup> groupList = Lists.newArrayList(group1);
            ZkUtil.instance.registerPersist(ZKPath.REDIS_SERVER.getPath(), Jsons.toJson(groupList));
        }
        RedisPathListener listener = new RedisPathListener();
        ZkUtil.instance.getCache().getListenable().addListener(listener);
        listener.initData(null);
    }
}
