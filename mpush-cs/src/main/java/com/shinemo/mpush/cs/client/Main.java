package com.shinemo.mpush.cs.client;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

import com.google.common.collect.Lists;
import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.core.client.ClientChannelHandler;
import com.shinemo.mpush.cs.ConnectionServerApplication;
import com.shinemo.mpush.netty.client.NettyClientFactory;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionClientMain main = new ConnectionClientMain();
		main.start();
		
		List<ConnectionServerApplication> serverList = main.getApplicationList();
		
		
		int index = (int) ((Math.random() % serverList.size()) * serverList.size());
		ConnectionServerApplication server = serverList.get(index);
		
		List<Client> clientList = Lists.newArrayList();
		
		for(int i = 0;i<100;i++){
			ClientChannelHandler handler = new ClientChannelHandler();
			
			final Client client = NettyClientFactory.INSTANCE.get(server.getIp(), server.getPort(), handler, true);
			clientList.add(client);
		}
		
		LockSupport.park();

	}

}
