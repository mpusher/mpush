package com.shinemo.mpush.tools.redis.consistenthash;

public class Node {
	
	private String ip; //机器ip
	private String name;//名字
	
	public Node(String ip, String name) {
		this.ip = ip;
		this.name = name;
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
