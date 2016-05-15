package com.mpush.zk;

import com.mpush.tools.config.ConfigCenter;

public class ZKConfig {
    public static final int ZK_MAX_RETRY = 3;
    public static final int ZK_MIN_TIME = 5000;
    public static final int ZK_MAX_TIME = 5000;
    public static final int ZK_SESSION_TIMEOUT = 5000;
    public static final int ZK_CONNECTION_TIMEOUT = 5000;
    public static final String ZK_DEFAULT_CACHE_PATH = "/";

    private String hosts;

    private String digest;

    private String namespace;

    private int maxRetry = ZK_MAX_RETRY;

    private int minTime = ZK_MIN_TIME;

    private int maxTime = ZK_MAX_TIME;

    private int sessionTimeout = ZK_SESSION_TIMEOUT;

    private int connectionTimeout = ZK_CONNECTION_TIMEOUT;

    private String localCachePath = ZK_DEFAULT_CACHE_PATH;

    public ZKConfig(String hosts) {
        this.hosts = hosts;
    }

    public static ZKConfig build(String hosts) {
        return new ZKConfig(hosts);
    }

    public String getHosts() {
        return hosts;
    }

    public ZKConfig setHosts(String hosts) {
        this.hosts = hosts;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public ZKConfig setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public ZKConfig setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    public int getMinTime() {
        return minTime;
    }

    public ZKConfig setMinTime(int minTime) {
        this.minTime = minTime;
        return this;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public ZKConfig setMaxTime(int maxTime) {
        this.maxTime = maxTime;
        return this;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public ZKConfig setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public ZKConfig setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public String getDigest() {
        return digest;
    }

    public ZKConfig setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public String getLocalCachePath() {
        return localCachePath;
    }

    public ZKConfig setLocalCachePath(String localCachePath) {
        this.localCachePath = localCachePath;
        return this;
    }

    @Override
    public String toString() {
        return "ZKConfig{" +
                "hosts='" + hosts + '\'' +
                ", digest='" + digest + '\'' +
                ", namespace='" + namespace + '\'' +
                ", maxRetry=" + maxRetry +
                ", minTime=" + minTime +
                ", maxTime=" + maxTime +
                ", sessionTimeout=" + sessionTimeout +
                ", connectionTimeout=" + connectionTimeout +
                ", localCachePath='" + localCachePath + '\'' +
                '}';
    }
}
