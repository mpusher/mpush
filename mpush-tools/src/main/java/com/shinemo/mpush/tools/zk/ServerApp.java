package com.shinemo.mpush.tools.zk;

import java.io.Serializable;

public class ServerApp implements Serializable{
	
	private static final long serialVersionUID = 5495972321679092837L;
	
	private String ip;
	private String port;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	
}
