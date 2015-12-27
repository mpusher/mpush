package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/22.
 */
public class SessionInfo {
    public final String osName;
    public final String osVersion;
    public final String clientVersion;
    public final String deviceId;
    public final byte[] sessionKey;
    public final byte[] iv;

    public SessionInfo(String osName, String osVersion, String clientVersion, String deviceId, byte[] sessionKey, byte[] iv) {
        this.osName = osName;
        this.osVersion = osVersion;
        this.clientVersion = clientVersion;
        this.deviceId = deviceId;
        this.sessionKey = sessionKey;
        this.iv = iv;
    }
}
