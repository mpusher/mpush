package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Created by ohun on 2015/12/24.
 */
public class HandShakeMessage extends BaseBufferBodyMessage {
    public String deviceId;
    public String osName;
    public String osVersion;
    public String clientVersion;
    public byte[] iv;
    public byte[] clientKey;
    public long timestamp;

    public HandShakeMessage(int sessionId, Connection connection) {
        super(new Packet(), connection);
        super.message.cmd = Command.Handshake.cmd;
        super.message.sessionId = sessionId;
    }

    public HandShakeMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        deviceId = decodeString(body);
        osName = decodeString(body);
        osVersion = decodeString(body);
        clientVersion = decodeString(body);
        iv = decodeBytes(body);
        clientKey = decodeBytes(body);
        timestamp = body.readLong();
    }

    public void encode(ByteBuf body) {
        encodeString(body, deviceId);
        encodeString(body, osName);
        encodeString(body, osVersion);
        encodeString(body, clientVersion);
        encodeBytes(body, iv);
        encodeBytes(body, clientKey);
        body.writeLong(timestamp);
    }

    public HandshakeSuccessMessage createSuccessMessage() {
        return new HandshakeSuccessMessage(createResponse(), getConnection());
    }
}
