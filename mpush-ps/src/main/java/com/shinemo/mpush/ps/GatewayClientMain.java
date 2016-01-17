//package com.shinemo.mpush.ps;
//
//
//
//import com.shinemo.mpush.api.Server;
//import com.shinemo.mpush.common.AbstractServer;
//import com.shinemo.mpush.common.app.impl.GatewayServerApplication;
//import com.shinemo.mpush.tools.Jsons;
//
//public class GatewayClientMain extends AbstractServer<GatewayServerApplication>{
//	
//	
//	private GatewayServerApplication gatewayServerApplication;
//	
//	public GatewayClientMain(){
//		
//		registerListener(new GatewayServerPathListener());
//		
//		connectionServerApplication = (ConnectionServerApplication)application;
//		gatewayServerApplication =  new GatewayServerApplication();
//		connectionServerApplication.setGatewayServerApplication(gatewayServerApplication);
//		gatewayServer = new GatewayServer(gatewayServerApplication.getPort());
//	}
//	
//	@Override
//	public Server getServer() {
//		final int port = application.getPort();
//        ConnectionServer connectionServer = new ConnectionServer(port);
//        return connectionServer;
//	}
//	
//
//}
