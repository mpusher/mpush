package com.shinemo.mpush.tools.zk;
import com.shinemo.mpush.tools.Constants;


public class ZkConfig {
	
    private final String ipLists;
    
    private final  String namespace;
    
    private final int maxRetry;
    
    private final int minTime;
    
    private final int maxTime;
    
    private final int sessionTimeout;
    
    private final int connectionTimeout;
    
    private final String digest;
    
    private final String localCachePath;

	public ZkConfig(String ipLists, String namespace) {
		this(ipLists, namespace, null);
	}
	
	public ZkConfig(String ipLists, String namespace,String digest) {
		this(ipLists, namespace, Constants.ZK_MAX_RETRY, Constants.ZK_MIN_TIME, Constants.ZK_MAX_TIME, Constants.ZK_SESSION_TIMEOUT, Constants.ZK_CONNECTION_TIMEOUT,digest,Constants.ZK_DEFAULT_CACHE_PATH);
	}
	
	public ZkConfig(String ipLists, String namespace, int maxRetry, int minTime, int maxTime, int sessionTimeout, int connectionTimeout,String digest,String localCachePath) {
		this.ipLists = ipLists;
		this.namespace = namespace;
		this.maxRetry = maxRetry;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.sessionTimeout = sessionTimeout;
		this.connectionTimeout = connectionTimeout;
		this.digest = digest;
		this.localCachePath = localCachePath;
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

	public String getLocalCachePath() {
		return localCachePath;
	}
	
}
