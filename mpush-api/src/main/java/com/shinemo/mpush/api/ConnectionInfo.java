package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/22.
 */
public class ConnectionInfo {
    public final String clientIp;
    public final int clientPort;
    public final String clientVersion;
    public final String deviceId;
    public final String desKey;

    public ConnectionInfo(String clientIp, int clientPort, String clientVersion, String deviceId, String desKey) {
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.clientVersion = clientVersion;
        this.deviceId = deviceId;
        this.desKey = desKey;
    }
}
