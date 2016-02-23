package com.shinemo.mpush.conn.client;

import com.shinemo.mpush.common.app.Application;
import com.shinemo.mpush.conn.client.GatewayServerApplication;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.zk.ZKPath;

public class ConnectionServerApplication extends Application{
	
	private transient GatewayServerApplication gatewayServerApplication;
	
	public ConnectionServerApplication() throws Exception {
		this(ConfigCenter.holder.connectionServerPort(),ZKPath.CONNECTION_SERVER.getWatchPath(),MPushUtil.getLocalIp(),MPushUtil.getExtranetAddress());
	}
	
	public ConnectionServerApplication(int port,String path,String ip,String extranetIp) {
		setPort(port);
		setServerRegisterZkPath(path);
		setIp(ip);
		setExtranetIp(extranetIp);
	}

	public GatewayServerApplication getGatewayServerApplication() {
		return gatewayServerApplication;
	}

	public void setGatewayServerApplication(GatewayServerApplication gatewayServerApplication) {
		this.gatewayServerApplication = gatewayServerApplication;
	}

}
