package com.shinemo.mpush.core;


/**
 * 系统配置 
 *
 */
public abstract class Application {
	
	private int port;
	
	private String serverRegisterZkPath;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServerRegisterZkPath() {
		return serverRegisterZkPath;
	}

	public void setServerRegisterZkPath(String serverRegisterZkPath) {
		this.serverRegisterZkPath = serverRegisterZkPath;
	}
	
}
