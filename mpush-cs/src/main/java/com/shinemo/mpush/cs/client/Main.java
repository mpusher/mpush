package com.shinemo.mpush.cs.client;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.cs.ConnectionServerApplication;
import com.shinemo.mpush.netty.client.NettyClientFactory;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionClientMain main = new ConnectionClientMain();
		main.start();
		
		List<ConnectionServerApplication> serverList = main.getApplicationList();
		
		
		int index = (int) ((Math.random() % serverList.size()) * serverList.size());
		ConnectionServerApplication server = serverList.get(index);
		ClientChannelHandler handler = new ClientChannelHandler();
		final Client client = NettyClientFactory.INSTANCE.get(server.getIp(), server.getPort(), handler);
		client.init();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				client.start();
			}
		});
		t.setDaemon(false);
		t.start();
		
		LockSupport.park();

	}

}
