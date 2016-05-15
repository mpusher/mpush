package com.mpush.tools.redis;

/**
 * redis 相关的配置信息
 *
 */
public class RedisNode {
	
	private String ip;
	private int port;
	private String password;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public RedisNode(String ip, int port, String password) {
		this.ip = ip;
		this.port = port;
		this.password = password;
	}
	@Override
	public String toString() {
		return "RedisNode [ip=" + ip + ", port=" + port + ", password=" + password + "]";
	}
	
}
