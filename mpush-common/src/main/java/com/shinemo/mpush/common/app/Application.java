package com.shinemo.mpush.common.app;


/**
 * 系统配置 
 *
 */
public abstract class Application {
	
	private String ip;
	
	private int port;
	
	private transient String serverRegisterZkPath;
	
	private String extranetIp;

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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getExtranetIp() {
		return extranetIp;
	}

	public void setExtranetIp(String extranetIp) {
		this.extranetIp = extranetIp;
	}
	
}
