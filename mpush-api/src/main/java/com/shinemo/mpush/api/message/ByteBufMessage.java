package com.shinemo.mpush.api.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by ohun on 2015/12/28.
 */
public abstract class ByteBufMessage extends BaseMessage {

    public ByteBufMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(byte[] body) {
        decode(Unpooled.wrappedBuffer(body));
    }

    @Override
    public byte[] encode() {
        ByteBuf body = Unpooled.buffer();
        encode(body);
        return body.array();
    }

    public abstract void decode(ByteBuf body);

    public abstract void encode(ByteBuf body);

    public void encodeString(ByteBuf body, String field) {
        if (field == null) {
            body.writeShort(0);
        } else {
            body.writeShort(field.length()).writeBytes(field.getBytes(Constants.UTF_8));
        }
    }

    public void encodeByte(ByteBuf body, byte field) {
        body.writeByte(field);
    }

    public void encodeInt(ByteBuf body, int field) {
        body.writeInt(field);
    }

    public void encodeLong(ByteBuf body, long field) {
        body.writeLong(field);
    }

    public void encodeBytes(ByteBuf body, byte[] field) {
        if (field == null || field.length == 0) {
            body.writeShort(0);
        } else {
            body.writeShort(field.length).writeBytes(field);
        }
    }

    public String decodeString(ByteBuf body) {
        byte[] bytes = decodeBytes(body);
        if (bytes == null) return null;
        return new String(bytes, Constants.UTF_8);
    }

    public byte[] decodeBytes(ByteBuf body) {
        int fieldLength = body.readShort();
        if (fieldLength == 0) return null;
        byte[] bytes = new byte[fieldLength];
        body.readBytes(bytes);
        return bytes;
    }

    public byte decodeByte(ByteBuf body) {
        return body.readByte();
    }

    public int decodeInt(ByteBuf body) {
        return body.readInt();
    }

    public long decodeLong(ByteBuf body) {
        return body.readLong();
    }
}
