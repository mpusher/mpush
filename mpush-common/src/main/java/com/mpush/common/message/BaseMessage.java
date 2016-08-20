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

import com.mpush.api.Message;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.protocol.Packet;
import com.mpush.tools.common.IOUtils;
import com.mpush.tools.config.CC;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ohun on 2015/12/28.
 *
 * @author ohun@live.cn
 */
public abstract class BaseMessage implements Message {
    private static final AtomicInteger ID_SEQ = new AtomicInteger();
    protected final Packet packet;
    protected final Connection connection;

    public BaseMessage(Packet packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
        decodeBody();
    }

    protected void decodeBody() {
        if (packet.body != null && packet.body.length > 0) {
            //1.解密
            byte[] tmp = packet.body;
            if (packet.hasFlag(Packet.FLAG_CRYPTO)) {
                if (connection.getSessionContext().cipher != null) {
                    tmp = connection.getSessionContext().cipher.decrypt(tmp);
                }
            }
            //2.解压
            if (packet.hasFlag(Packet.FLAG_COMPRESS)) {
                tmp = IOUtils.decompress(tmp);
            }

            if (tmp.length == 0) {
                throw new RuntimeException("message decode ex");
            }

            packet.body = tmp;
            decode(packet.body);
        }
    }

    protected void encodeBody() {
        byte[] tmp = encode();
        if (tmp != null && tmp.length > 0) {
            //1.压缩
            if (tmp.length > CC.mp.core.compress_threshold) {
                byte[] result = IOUtils.compress(tmp);
                if (result.length > 0) {
                    tmp = result;
                    packet.addFlag(Packet.FLAG_COMPRESS);
                }
            }

            //2.加密
            SessionContext context = connection.getSessionContext();
            if (context.cipher != null) {
                byte[] result = context.cipher.encrypt(tmp);
                if (result.length > 0) {
                    tmp = result;
                    packet.addFlag(Packet.FLAG_CRYPTO);
                }
            }
            packet.body = tmp;
        }
    }

    public abstract void decode(byte[] body);

    public abstract byte[] encode();

    public Packet createResponse() {
        return new Packet(packet.cmd, packet.sessionId);
    }

    @Override
    public Packet getPacket() {
        return packet;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void send(ChannelFutureListener listener) {
        encodeBody();
        connection.send(packet, listener);
    }

    @Override
    public void sendRaw(ChannelFutureListener listener) {
        packet.body = encode();
        connection.send(packet, listener);
    }

    public void send() {
        send(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendRaw() {
        sendRaw(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void close() {
        send(ChannelFutureListener.CLOSE);
    }

    protected static int genSessionId() {
        return ID_SEQ.incrementAndGet();
    }

    public int getSessionId() {
        return packet.sessionId;
    }

    @Override
    public abstract String toString();
}
