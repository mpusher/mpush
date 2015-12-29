package com.shinemo.mpush.api;

import com.shinemo.mpush.tools.MPushUtil;

/**
 * Created by ohun on 2015/12/23.
 */
public final class UserConnConfig {
    private String host;
    private String osName;
    private String clientVersion;
    private String deviceId;


    public String getDeviceId() {
        return deviceId;
    }

    public UserConnConfig setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public static UserConnConfig from(SessionContext context) {
        UserConnConfig config = new UserConnConfig();
        config.osName = context.osName;
        config.clientVersion = context.clientVersion;
        config.deviceId = context.deviceId;
        config.host = MPushUtil.getLocalIp();
        return config;
    }
}
