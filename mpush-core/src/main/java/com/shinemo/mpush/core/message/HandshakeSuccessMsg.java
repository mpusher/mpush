package com.shinemo.mpush.core.message;

/**
 * Created by ohun on 2015/12/27.
 */
public class HandshakeSuccessMsg {
    public byte[] serverKey;
    public String serverHost;
    public long serverTime;
    public int heartbeat;
    public String sessionId;
    public long expireTime;
}
