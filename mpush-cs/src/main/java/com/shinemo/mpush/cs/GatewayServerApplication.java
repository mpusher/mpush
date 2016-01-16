package com.shinemo.mpush.cs;

import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.zk.ZKPath;


public class GatewayServerApplication extends ConnectionServerApplication{

	public GatewayServerApplication() {
		this(ConfigCenter.holder.gatewayServerPort(),ZKPath.GATEWAY_SERVER.getWatchPath(),MPushUtil.getLocalIp());
	}
	
	public GatewayServerApplication(int port,String path,String ip) {
		setPort(port);
		setServerRegisterZkPath(path);
		setIp(ip);
	}
	
}
