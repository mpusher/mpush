package com.shinemo.mpush.cs.client;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

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
		
		byte[] clientKey = null;
		byte[] iv = null;
		String clientVersion = null;
        String deviceId = null;
        String osName = null;
        String osVersion = null;
        String userId = null;
        String cipher = null;
		
		for(int i = 0;i<100;i++){
			ClientChannelHandler handler = new ClientChannelHandler();
			NettyClientFactory.INSTANCE.createSecurityClient(server.getIp(), server.getPort(), handler, clientKey, iv, clientVersion, deviceId, osName, osVersion, userId, cipher);
		}
		
		LockSupport.park();

	}

}
