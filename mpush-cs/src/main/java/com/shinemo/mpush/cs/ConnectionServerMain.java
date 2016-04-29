package com.shinemo.mpush.cs;


import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.common.AbstractServer;
import com.shinemo.mpush.common.manage.user.UserManager;
import com.shinemo.mpush.conn.client.ConnectionServerApplication;
import com.shinemo.mpush.conn.client.GatewayServerApplication;
import com.shinemo.mpush.core.server.AdminServer;
import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.core.server.GatewayServer;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.dns.manage.DnsMappingManage;

public class ConnectionServerMain extends AbstractServer<ConnectionServerApplication> {

    private Server gatewayServer;

    private Server adminServer;

    private ConnectionServerApplication connectionServerApplication;

    private GatewayServerApplication gatewayServerApplication;

    public ConnectionServerMain() {

//		registerListener(new ConnectionServerPathListener());
//		registerListener(new GatewayServerPathListener());

        connectionServerApplication = (ConnectionServerApplication) application;
        gatewayServerApplication = new GatewayServerApplication();
        connectionServerApplication.setGatewayServerApplication(gatewayServerApplication);
        gatewayServer = new GatewayServer(gatewayServerApplication.getPort());
        adminServer = new AdminServer(ConfigCenter.holder.adminPort());
    }

    @Override
    public void start() {
        super.start();
        startServer(gatewayServer, gatewayServerApplication.getServerRegisterZkPath(), Jsons.toJson(gatewayServerApplication));
//		registerServerToZk(gatewayServerApplication.getServerRegisterZkPath(), Jsons.toJson(gatewayServerApplication));
        startServer(adminServer);

        UserManager.INSTANCE.clearUserOnlineData();

        if (ConfigCenter.holder.httpProxyEnable()) {
            DnsMappingManage.holder.init();
        }
    }


    @Override
    public Server getServer() {
        int port = application.getPort();
        return new ConnectionServer(port);
    }

    @Override
    public void stop() {
        super.stop();
        stopServer(gatewayServer);
        stopServer(adminServer);
    }
}
