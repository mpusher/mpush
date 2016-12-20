/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.common.message;

import com.mpush.api.Constants;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by ohun on 2015/12/28.
 *
 * @author ohun@live.cn
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
        ByteBuf body = connection.getChannel().alloc().heapBuffer();
        try {
            encode(body);
            byte[] bytes = new byte[body.readableBytes()];
            body.readBytes(bytes);
            return bytes;
        } finally {
            body.release();
        }
    }

    public abstract void decode(ByteBuf body);

    public abstract void encode(ByteBuf body);

    public void encodeString(ByteBuf body, String field) {
        encodeBytes(body, field == null ? null : field.getBytes(Constants.UTF_8));
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
        } else if (field.length < Short.MAX_VALUE) {
            body.writeShort(field.length).writeBytes(field);
        } else {
            body.writeShort(Short.MAX_VALUE).writeInt(field.length - Short.MAX_VALUE).writeBytes(field);
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
        if (fieldLength == Short.MAX_VALUE) {
            fieldLength += body.readInt();
        }
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
