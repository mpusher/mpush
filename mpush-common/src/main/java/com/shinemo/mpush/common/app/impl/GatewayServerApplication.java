package com.shinemo.mpush.common.app.impl;

import com.shinemo.mpush.common.app.Application;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.zk.ZKPath;


public class GatewayServerApplication extends Application{

	public GatewayServerApplication() {
		this(ConfigCenter.holder.gatewayServerPort(),ZKPath.GATEWAY_SERVER.getWatchPath(),MPushUtil.getLocalIp());
	}
	
	public GatewayServerApplication(int port,String path,String ip) {
		setPort(port);
		setServerRegisterZkPath(path);
		setIp(ip);
	}
	
}
