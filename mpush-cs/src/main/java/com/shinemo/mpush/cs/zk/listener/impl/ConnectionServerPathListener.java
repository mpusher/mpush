package com.shinemo.mpush.cs.zk.listener.impl;


import com.shinemo.mpush.cs.ConnectionServerApplication;
import com.shinemo.mpush.cs.manage.ServerManage;
import com.shinemo.mpush.cs.zk.listener.AbstractDataChangeListener;
import com.shinemo.mpush.tools.spi.ServiceContainer;
import com.shinemo.mpush.tools.zk.ZKPath;

/**
 * connection server 应用  监控
 * 
 */
public class ConnectionServerPathListener extends AbstractDataChangeListener<ConnectionServerApplication>{
	
	@SuppressWarnings("unchecked")
	private ServerManage<ConnectionServerApplication> connectionServerManage = ServiceContainer.getInstance(ServerManage.class);
	
	@Override
	public String listenerPath() {
		return ZKPath.CONNECTION_SERVER.getWatchPath();
	}

	@Override
	public String getRegisterPath() {
		return ZKPath.CONNECTION_SERVER.getPath();
	}

	@Override
	public ServerManage<ConnectionServerApplication> getServerManage() {
		return connectionServerManage;
	}

}
