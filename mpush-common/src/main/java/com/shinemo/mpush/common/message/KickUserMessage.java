package com.shinemo.mpush.common.message;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

import static com.shinemo.mpush.api.protocol.Command.KICK;

/**
 * Created by ohun on 2015/12/29.
 */
public class KickUserMessage extends ByteBufMessage {
    public String deviceId;
    public String userId;

    public KickUserMessage(Connection connection) {
        super(new Packet(KICK), connection);
    }

    public KickUserMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        deviceId = decodeString(body);
        userId = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, deviceId);
        encodeString(body, userId);
    }

    @Override
    public String toString() {
        return "KickUserMessage{" +
                "deviceId='" + deviceId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
