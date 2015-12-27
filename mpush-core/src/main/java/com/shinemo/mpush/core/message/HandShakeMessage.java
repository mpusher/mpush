package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Message;

/**
 * Created by ohun on 2015/12/24.
 */
public class HandshakeMessage implements Message {
    public String deviceId;
    public String osName;
    public String osVersion;
    public String clientVersion;
    public byte[] clientKey;
    public byte[] iv;
    public long timestamp;
}
