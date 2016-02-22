package com.shinemo.mpush.cs;



import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.common.AbstractServer;
import com.shinemo.mpush.conn.client.GatewayServerApplication;
import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.core.server.GatewayServer;
import com.shinemo.mpush.tools.Jsons;

public class ConnectionServerMain extends AbstractServer<ConnectionServerApplication>{
	
	private Server gatewayServer;
	
	private ConnectionServerApplication  connectionServerApplication;
	
	private GatewayServerApplication gatewayServerApplication;
	
	public ConnectionServerMain(){
		
//		registerListener(new ConnectionServerPathListener());
//		registerListener(new GatewayServerPathListener());
		
		connectionServerApplication = (ConnectionServerApplication)application;
		gatewayServerApplication =  new GatewayServerApplication();
		connectionServerApplication.setGatewayServerApplication(gatewayServerApplication);
		gatewayServer = new GatewayServer(gatewayServerApplication.getPort());
	}
	
	@Override
	public void start() {
		super.start();
		startServer(gatewayServer);
		registerServerToZk(gatewayServerApplication.getServerRegisterZkPath(), Jsons.toJson(gatewayServerApplication));
	}
	

	@Override
	public Server getServer() {
		final int port = application.getPort();
        ConnectionServer connectionServer = new ConnectionServer(port);
        return connectionServer;
	}
	
	@Override
	public void stop() {
		super.stop();
		stopServer(gatewayServer);
	}
	

}
