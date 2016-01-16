package com.shinemo.mpush.cs;

import com.shinemo.mpush.common.Application;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.zk.ZKPath;

public class GatewayServerApplication extends Application{
	
	public GatewayServerApplication() {
		setPort(ConfigCenter.holder.gatewayServerPort());
		setServerRegisterZkPath(ZKPath.GATEWAY_SERVER.getWatchPath());
		setIp(MPushUtil.getLocalIp());
	}

}
