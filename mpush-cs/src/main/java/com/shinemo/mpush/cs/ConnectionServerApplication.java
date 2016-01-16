package com.shinemo.mpush.cs;

import com.shinemo.mpush.common.Application;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.zk.ZKPath;

public class ConnectionServerApplication extends Application{
	
	private transient GatewayServerApplication gatewayServerApplication;
	
	public ConnectionServerApplication() {
		this(ConfigCenter.holder.connectionServerPort(),ZKPath.CONNECTION_SERVER.getWatchPath(),MPushUtil.getLocalIp());
	}
	
	public ConnectionServerApplication(int port,String path,String ip) {
		setPort(port);
		setServerRegisterZkPath(path);
		setIp(ip);
	}

	public GatewayServerApplication getGatewayServerApplication() {
		return gatewayServerApplication;
	}

	public void setGatewayServerApplication(GatewayServerApplication gatewayServerApplication) {
		this.gatewayServerApplication = gatewayServerApplication;
	}

}
