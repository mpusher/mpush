package com.shinemo.mpush.cs;



import com.shinemo.mpush.api.RedisKey;
import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.common.AbstractServer;
import com.shinemo.mpush.conn.client.ConnectionServerApplication;
import com.shinemo.mpush.conn.client.GatewayServerApplication;
import com.shinemo.mpush.core.server.AdminServer;
import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.core.server.GatewayServer;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.dns.manage.DnsMappingManage;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

public class ConnectionServerMain extends AbstractServer<ConnectionServerApplication>{
	
	private Server gatewayServer;
	
	private Server adminServer;
	
	private ConnectionServerApplication  connectionServerApplication;
	
	private GatewayServerApplication gatewayServerApplication;
	
	public ConnectionServerMain(){
		
//		registerListener(new ConnectionServerPathListener());
//		registerListener(new GatewayServerPathListener());
		
		connectionServerApplication = (ConnectionServerApplication)application;
		gatewayServerApplication =  new GatewayServerApplication();
		connectionServerApplication.setGatewayServerApplication(gatewayServerApplication);
		gatewayServer = new GatewayServer(gatewayServerApplication.getPort());
		adminServer = new AdminServer(ConfigCenter.holder.adminPort());
	}
	
	@Override
	public void start() {
		super.start();
		startServer(gatewayServer,gatewayServerApplication.getServerRegisterZkPath(),Jsons.toJson(gatewayServerApplication));
//		registerServerToZk(gatewayServerApplication.getServerRegisterZkPath(), Jsons.toJson(gatewayServerApplication));
		startServer(adminServer);
		
		RedisManage.del(RedisKey.getUserOnlineKey(MPushUtil.getExtranetIp()));
		DnsMappingManage.holder.init();
		
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
		stopServer(adminServer);
	}
	

}
