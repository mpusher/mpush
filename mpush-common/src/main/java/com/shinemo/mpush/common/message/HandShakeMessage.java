package com.shinemo.mpush.common.message;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Created by ohun on 2015/12/24.
 */
public final class HandshakeMessage extends ByteBufMessage {
    public String deviceId;
    public String osName;
    public String osVersion;
    public String clientVersion;
    public byte[] iv;
    public byte[] clientKey;
    public long timestamp;

    public HandshakeMessage(Connection connection) {
        super(new Packet(Command.HANDSHAKE.cmd, genSessionId()), connection);
    }

    public HandshakeMessage(Packet message, Connection connection) {
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
        timestamp = decodeLong(body);
    }

    public void encode(ByteBuf body) {
        encodeString(body, deviceId);
        encodeString(body, osName);
        encodeString(body, osVersion);
        encodeString(body, clientVersion);
        encodeBytes(body, iv);
        encodeBytes(body, clientKey);
        encodeLong(body, timestamp);
    }
}
