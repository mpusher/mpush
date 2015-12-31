package com.shinemo.mpush.tools.zk;

import java.io.Serializable;

public class ServerApp implements Serializable{
	
	private static final long serialVersionUID = 5495972321679092837L;
	
	private final String ip;
	private final String port;
	
	public ServerApp(String ip, String port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getIp() {
		return ip;
	}
	public String getPort() {
		return port;
	}
	
}
