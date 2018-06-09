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

import com.mpush.api.connection.Cipher;
import com.mpush.api.connection.Connection;
import com.mpush.api.message.Message;
import com.mpush.api.protocol.Packet;
import com.mpush.tools.Jsons;
import com.mpush.tools.common.IOUtils;
import com.mpush.tools.common.Profiler;
import com.mpush.tools.config.CC;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ohun on 2015/12/28.
 *
 * @author ohun@live.cn
 */
public abstract class BaseMessage implements Message {
    private static final byte STATUS_DECODED = 1;
    private static final byte STATUS_ENCODED = 2;
    private static final AtomicInteger ID_SEQ = new AtomicInteger();
    transient protected Packet packet;
    transient protected Connection connection;
    transient private byte status = 0;

    public BaseMessage(Packet packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
    }

    @Override
    public void decodeBody() {
        if ((status & STATUS_DECODED) == 0) {
            status |= STATUS_DECODED;

            if (packet.getBodyLength() > 0) {
                if (packet.hasFlag(Packet.FLAG_JSON_BODY)) {
                    decodeJsonBody0();
                } else {
                    decodeBinaryBody0();
                }
            }

        }
    }

    @Override
    public void encodeBody() {
        if ((status & STATUS_ENCODED) == 0) {
            status |= STATUS_ENCODED;

            if (packet.hasFlag(Packet.FLAG_JSON_BODY)) {
                encodeJsonBody0();
            } else {
                encodeBinaryBody0();
            }
        }

    }

    private void decodeBinaryBody0() {
        //1.解密
        byte[] tmp = packet.body;
        if (packet.hasFlag(Packet.FLAG_CRYPTO)) {
            if (getCipher() != null) {
                tmp = getCipher().decrypt(tmp);
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
        Profiler.enter("time cost on [body decode]");
        decode(packet.body);
        Profiler.release();
        packet.body = null;// 释放内存
    }

    private void encodeBinaryBody0() {
        Profiler.enter("time cost on [body encode]");
        byte[] tmp = encode();
        Profiler.release();
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
            Cipher cipher = getCipher();
            if (cipher != null) {
                byte[] result = cipher.encrypt(tmp);
                if (result.length > 0) {
                    tmp = result;
                    packet.addFlag(Packet.FLAG_CRYPTO);
                }
            }
            packet.body = tmp;
        }
    }

    private void decodeJsonBody0() {
        Map<String, Object> body = packet.getBody();
        decodeJsonBody(body);
    }

    private void encodeJsonBody0() {
        packet.setBody(encodeJsonBody());
    }

    private void encodeJsonStringBody0() {
        packet.setBody(encodeJsonStringBody());
    }

    protected String encodeJsonStringBody() {
        return Jsons.toJson(this);
    }

    private void encodeBodyRaw() {
        if ((status & STATUS_ENCODED) == 0) {
            status |= STATUS_ENCODED;

            if (packet.hasFlag(Packet.FLAG_JSON_BODY)) {
                encodeJsonBody0();
            } else {
                packet.body = encode();
            }
        }
    }

    public abstract void decode(byte[] body);

    public abstract byte[] encode();

    protected void decodeJsonBody(Map<String, Object> body) {

    }

    protected Map<String, Object> encodeJsonBody() {
        return null;
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
        encodeBodyRaw();
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

    public BaseMessage setRecipient(InetSocketAddress recipient) {
        packet.setRecipient(recipient);
        return this;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public ScheduledExecutorService getExecutor() {
        return connection.getChannel().eventLoop();
    }

    public void runInRequestThread(Runnable runnable) {
        connection.getChannel().eventLoop().execute(runnable);
    }

    protected Cipher getCipher() {
        return connection.getSessionContext().cipher;
    }

    @Override
    public abstract String toString();
}
