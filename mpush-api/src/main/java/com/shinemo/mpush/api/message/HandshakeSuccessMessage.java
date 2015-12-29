package com.shinemo.mpush.api.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Created by ohun on 2015/12/27.
 */
public final class HandshakeSuccessMessage extends ByteBufMessage {
    public byte[] serverKey;
    public String serverHost;
    public long serverTime;
    public int heartbeat;
    public String sessionId;
    public long expireTime;

    public HandshakeSuccessMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        serverKey = decodeBytes(body);
        serverHost = decodeString(body);
        serverTime = decodeLong(body);
        heartbeat = decodeInt(body);
        sessionId = decodeString(body);
        expireTime = decodeLong(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeBytes(body, serverKey);
        encodeString(body, serverHost);
        encodeLong(body, serverTime);
        encodeInt(body, heartbeat);
        encodeString(body, sessionId);
        encodeLong(body, expireTime);
    }

    public static HandshakeSuccessMessage from(BaseMessage src) {
        return new HandshakeSuccessMessage(src.createResponse(), src.connection);
    }

    public HandshakeSuccessMessage setServerKey(byte[] serverKey) {
        this.serverKey = serverKey;
        return this;
    }

    public HandshakeSuccessMessage setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public HandshakeSuccessMessage setServerTime(long serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public HandshakeSuccessMessage setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    public HandshakeSuccessMessage setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public HandshakeSuccessMessage setExpireTime(long expireTime) {
        this.expireTime = expireTime;
        return this;
    }
}
