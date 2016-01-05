package com.shinemo.mpush.core;

import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.core.server.GatewayServer;
import com.shinemo.mpush.tools.ConfigCenter;
import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

import java.io.IOException;

/**
 * Created by ohun on 2016/1/5.
 */
public final class App {
    private static final App APP = new App();

    public static void main(String[] args) throws Exception {
        APP.init();
        APP.startRedisClient();
        APP.startConnectionServer();
        APP.startGatewayServer();
        APP.startZKClient();
    }

    private void init() throws IOException {
        ConfigCenter.INSTANCE.init();
    }

    public void startRedisClient() {

    }

    public void startConnectionServer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int port = ConfigCenter.INSTANCE.getConnectionServerPort();
                ConnectionServer server = new ConnectionServer(port);
                server.init();
                server.start();
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
                int port = ConfigCenter.INSTANCE.getGatewayServerPort();
                GatewayServer server = new GatewayServer(port);
                server.init();
                server.start();
            }
        });
        t.setDaemon(false);
        t.setName("gateway-server-thread");
        t.start();
    }

    public void startZKClient() {
        //register remote ip for allocate
        //register local ip for push client
        ServerApp app = new ServerApp(InetAddressUtil.getInetAddress(),
                Integer.toString(ConfigCenter.INSTANCE.getConnectionServerPort()));
        ServerManage manage = new ServerManage(app);
        manage.start();
    }
}
