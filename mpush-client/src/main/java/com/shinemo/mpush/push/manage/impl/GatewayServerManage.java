package com.shinemo.mpush.push.manage.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.common.app.impl.GatewayServerApplication;
import com.shinemo.mpush.common.manage.ServerManage;
import com.shinemo.mpush.netty.client.NettyClient;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import com.shinemo.mpush.push.client.ClientChannelHandler;

public class GatewayServerManage implements ServerManage<GatewayServerApplication>{

	private static final Logger log = LoggerFactory.getLogger(GatewayServerManage.class);

	private static Map<String,GatewayServerApplication> holder = Maps.newConcurrentMap();
	
	private final Map<GatewayServerApplication, Client> application2Client = Maps.newConcurrentMap();
    
    private final Map<String,Client> ip2Client = Maps.newConcurrentMap();
	
	@Override
	public void addOrUpdate(String fullPath,GatewayServerApplication application){
		holder.put(fullPath, application);
		try{
			Client client = new NettyClient(application.getIp(), application.getPort());
			ClientChannelHandler handler = new ClientChannelHandler(client);
			NettyClientFactory.INSTANCE.create(handler);
			application2Client.put(application, client);
			ip2Client.put(application.getIp(), client);
		}catch(Exception e){
			
		}
		printList();
	}
	
	@Override
	public void remove(String fullPath){
		GatewayServerApplication application = get(fullPath);
		if(application!=null){
			Client client = application2Client.get(application);
			if(client!=null){
				client.stop();
			}
		}
		ip2Client.remove(application.getIp()+":"+application.getPort());
		holder.remove(fullPath);
		printList();
	}
	
	@Override
	public Collection<GatewayServerApplication> getList() {
		return Collections.unmodifiableCollection(holder.values());
	}
	
	private void printList(){
		for(GatewayServerApplication app:holder.values()){
			log.warn(ToStringBuilder.reflectionToString(app, ToStringStyle.DEFAULT_STYLE));
		}
	}
	
	public GatewayServerApplication get(String fullpath){
		return holder.get(fullpath);
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

