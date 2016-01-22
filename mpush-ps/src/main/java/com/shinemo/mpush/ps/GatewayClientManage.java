package com.shinemo.mpush.ps;

import java.util.Map;

import com.google.common.collect.Maps;
import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.common.app.impl.GatewayServerApplication;
import com.shinemo.mpush.common.manage.impl.GatewayServerManage;
import com.shinemo.mpush.netty.client.NettyClientFactory;

public class GatewayClientManage extends GatewayServerManage{
	
    private final Map<GatewayServerApplication, Client> application2Client = Maps.newConcurrentMap();
    
    private final Map<String,Client> ip2Client = Maps.newConcurrentMap();
	
	@Override
	public void addOrUpdate(String fullPath, GatewayServerApplication application) {
		super.addOrUpdate(fullPath, application);
		try{
			Client client = NettyClientFactory.INSTANCE.createClient(application.getIp(), application.getPort(), new GatewayClientChannelHandler(), false);
			application2Client.put(application, client);
			ip2Client.put(application.getIp()+":"+application.getPort(), client);
		}catch(Exception e){
			
		}
	
	}
	

	@Override
	public void remove(String fullPath) {
		GatewayServerApplication application = super.get(fullPath);
		super.remove(fullPath);
		
		if(application!=null){
			Client client = application2Client.get(application);
			if(client!=null){
				client.stop();
			}
		}
		ip2Client.remove(application.getIp()+":"+application.getPort());
	}
	
	public Client getClient(GatewayServerApplication application){
		return application2Client.get(application);
	}
	
	public Connection getConnection(String ipAndPort) {
        Client client = ip2Client.get(ipAndPort);
        if (client == null) return null;
        return client.getConnection();
    }


}
