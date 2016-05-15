package com.mpush.api.router;

import com.mpush.api.connection.SessionContext;

/**
 * Created by ohun on 2015/12/23.
 */
public final class ClientLocation {

    /**
     * 长链接所在的机器IP
     */
    private String host;

    /**
     * 客户端系统类型
     */
    private String osName;

    /**
     * 客户端版本
     */
    private String clientVersion;

    /**
     * 客户端设备ID
     */
    private String deviceId;


    public String getDeviceId() {
        return deviceId;
    }

    public ClientLocation setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }


    public String getHost() {
        return host;
    }

    public ClientLocation setHost(String host) {
        this.host = host;
        return this;
    }

    public String getOsName() {
        return osName;
    }

    public ClientLocation setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public ClientLocation setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public static ClientLocation from(SessionContext context) {
        ClientLocation config = new ClientLocation();
        config.osName = context.osName;
        config.clientVersion = context.clientVersion;
        config.deviceId = context.deviceId;
        return config;
    }

    @Override
    public String toString() {
        return "ClientLocation{" +
                "host='" + host + '\'' +
                ", osName='" + osName + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
