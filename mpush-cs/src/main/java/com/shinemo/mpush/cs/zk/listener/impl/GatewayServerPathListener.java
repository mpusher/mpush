package com.shinemo.mpush.cs.zk.listener.impl;

import com.shinemo.mpush.cs.GatewayServerApplication;
import com.shinemo.mpush.cs.manage.ServerManage;
import com.shinemo.mpush.cs.zk.listener.AbstractDataChangeListener;
import com.shinemo.mpush.tools.spi.ServiceContainer;
import com.shinemo.mpush.tools.zk.ZKPath;

/**
 * connection server 应用  监控
 * 
 */
public class GatewayServerPathListener extends AbstractDataChangeListener<GatewayServerApplication>{
	
	@SuppressWarnings("unchecked")
	private static ServerManage<GatewayServerApplication> gatewayServerManage = ServiceContainer.getInstance(ServerManage.class, "gatewayServerManage");
	

	@Override
	public String listenerPath() {
		return ZKPath.GATEWAY_SERVER.getWatchPath();
	}

	@Override
	public ServerManage<GatewayServerApplication> getServerManage() {
		return gatewayServerManage;
	}

	@Override
	public String getRegisterPath() {
		return ZKPath.GATEWAY_SERVER.getPath();
	}

	

}
