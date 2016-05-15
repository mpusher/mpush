package com.mpush;


import com.mpush.api.Server;
import com.mpush.boot.*;
import com.mpush.core.server.AdminServer;
import com.mpush.core.server.ConnectionServer;
import com.mpush.core.server.GatewayServer;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.zk.ZKServerNode;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class ServerLauncher {
    private final ZKServerNode csNode = ZKServerNode.csNode();

    private final ZKServerNode gsNode = ZKServerNode.gsNode();

    private final Server connectServer = new ConnectionServer(csNode.getPort());

    private final Server gatewayServer = new GatewayServer(gsNode.getPort());

    private final Server adminServer = new AdminServer(ConfigCenter.I.adminPort());


    public void start() {
        BootChain chain = BootChain.chain();
        chain.boot()
                .setNext(new RedisBoot())
                .setNext(new ZKBoot())
                .setNext(new ServerBoot(connectServer, csNode))
                .setNext(new ServerBoot(gatewayServer, gsNode))
                .setNext(new ServerBoot(adminServer, null))
                .setNext(new HttpProxyBoot())
                .setNext(new MonitorBoot())
                .setNext(new LastBoot());
        chain.run();
    }

    public void stop() {
        stopServer(gatewayServer);
        stopServer(gatewayServer);
        stopServer(adminServer);
    }

    private void stopServer(Server server) {
        if (server != null) {
            server.stop(null);
        }
    }
}
