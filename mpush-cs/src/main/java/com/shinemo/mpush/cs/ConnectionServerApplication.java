package com.shinemo.mpush.cs;

import com.shinemo.mpush.core.Application;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.zk.ZKPath;

public class ConnectionServerApplication extends Application{
	
	
	public ConnectionServerApplication() {
		setPort(ConfigCenter.holder.connectionServerPort());
		setServerRegisterZkPath(ZKPath.CONNECTION_SERVER.getWatchPath());
	}

}
