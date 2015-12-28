package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/22.
 */
public class SessionContext {
    public String osName;
    public String osVersion;
    public String clientVersion;
    public String deviceId;
    public byte[] sessionKey;
    public byte[] iv;
    public Cipher cipher;

    public SessionContext() {

    }

    public SessionContext(String osName, String osVersion, String clientVersion, String deviceId, byte[] sessionKey, byte[] iv) {
        this.osName = osName;
        this.osVersion = osVersion;
        this.clientVersion = clientVersion;
        this.deviceId = deviceId;
        this.sessionKey = sessionKey;
        this.iv = iv;
    }

    public void changeCipher(Cipher cipher) {
        this.cipher = cipher;
    }
}
