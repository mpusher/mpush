package com.shinemo.mpush.tools.zk;

public class ZkConfig {
	
    private final String ipLists;
    
    private final  String namespace;
    
    private final int maxRetry;
    
    private final int minTime;
    
    private final int maxTime;
    
    private final int sessionTimeout;
    
    private final int connectionTimeout;
    
    private final String digest;

	public ZkConfig(String ipLists, String namespace, int maxRetry, int minTime, int maxTime, int sessionTimeout, int connectionTimeout,String digest) {
		this.ipLists = ipLists;
		this.namespace = namespace;
		this.maxRetry = maxRetry;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.sessionTimeout = sessionTimeout;
		this.connectionTimeout = connectionTimeout;
		this.digest = digest;
	}

	public String getIpLists() {
		return ipLists;
	}

	public String getNamespace() {
		return namespace;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public int getMinTime() {
		return minTime;
	}

	public int getMaxTime() {
		return maxTime;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public String getDigest() {
		return digest;
	}
    
}
