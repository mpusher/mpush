package com.mpush.tools.dns;


public class DnsMapping {

	private String ip;
	private int port;
	
	public DnsMapping(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getIp() {
		return ip;
	}
	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return ip+":"+port;
	}
}
