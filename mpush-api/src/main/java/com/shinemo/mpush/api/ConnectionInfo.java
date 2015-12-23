package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/22.
 */
public class ConnectionInfo {
    public final String os;
    public final String clientVer;
    public final String deviceId;
    public final String desKey;

    public ConnectionInfo(String os, String clientVer, String deviceId, String desKey) {
        this.os = os;
        this.clientVer = clientVer;
        this.deviceId = deviceId;
        this.desKey = desKey;
    }
}
