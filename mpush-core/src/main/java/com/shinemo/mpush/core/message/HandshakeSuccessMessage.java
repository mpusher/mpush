package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Created by ohun on 2015/12/27.
 */
public class HandshakeSuccessMessage extends BaseBufferBodyMessage {
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
        serverTime = body.readLong();
        heartbeat = body.readInt();
        sessionId = decodeString(body);
        expireTime = body.readLong();
    }

    @Override
    public void encode(ByteBuf body) {
        encodeBytes(body, serverKey);
        encodeString(body, serverHost);
        body.writeLong(serverTime);
        body.writeInt(heartbeat);
        encodeString(body, sessionId);
        body.writeLong(expireTime);
    }
}
