package com.shinemo.mpush.tools.zk.manage;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.queue.Provider;

public class KickProviderQueueManage {

	private static Map<ServerApp,Provider<ServerApp>> providers = Maps.newConcurrentMap();
	
	private ServerManage serverManage;
	
	public KickProviderQueueManage(ServerManage serverManage) {
		this.serverManage = serverManage;
		Iterator<ServerApp> iterator = ServerAppManage.instance.getAppList().iterator();
		while (iterator.hasNext()) {
			ServerApp app = iterator.next();
			if(!app.getIp().equals(this.serverManage.getServerApp().getIp())){
				Provider<ServerApp> provider = new Provider<ServerApp>(PathEnum.CONNECTION_SERVER_KICK.getPathByIp(app.getIp()), ServerApp.class);
				providers.put(app, provider);
			}
		}
	}
	
	public void start() throws Exception{
		Iterator<Provider<ServerApp>> iterator = providers.values().iterator();
		while(iterator.hasNext()){
			Provider<ServerApp> provider = iterator.next();
			provider.start();
		}
	}
	
	
}
