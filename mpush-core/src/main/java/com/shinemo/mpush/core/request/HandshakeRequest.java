package com.shinemo.mpush.core.request;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.message.NettyRequest;

/**
 * Created by ohun on 2015/12/27.
 */
public class HandshakeRequest extends NettyRequest {
    public String deviceId;
    public String osName;
    public String osVersion;
    public String clientVersion;
    public byte[] clientKey;
    public long timestamp;

    public HandshakeRequest(Packet message, Connection connection) {
        super(message, connection);
    }
}
