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

package com.mpush.api.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;

/**
 * Created by ohun on 2015/12/19.
 * length(4)+cmd(1)+cc(2)+flags(1)+sessionId(4)+lrc(1)+body(n)
 *
 * @author ohun@live.cn
 */
public class Packet {
    public static final int HEADER_LEN = 13;

    public static final byte FLAG_CRYPTO = 0x01;
    public static final byte FLAG_COMPRESS = 0x02;
    public static final byte FLAG_BIZ_ACK = 0x04;
    public static final byte FLAG_AUTO_ACK = 0x08;

    public static final byte HB_PACKET_BYTE = -33;
    public static final byte[] HB_PACKET_BYTES = new byte[]{HB_PACKET_BYTE};
    public static final Packet HB_PACKET = new Packet(Command.HEARTBEAT);

    public byte cmd; //命令
    public short cc; //校验码 暂时没有用到
    public byte flags; //特性，如是否加密，是否压缩等
    public int sessionId; // 会话id。客户端生成。
    public byte lrc; // 校验，纵向冗余校验。只校验head
    public byte[] body;

    public Packet(byte cmd) {
        this.cmd = cmd;
    }

    public Packet(byte cmd, int sessionId) {
        this.cmd = cmd;
        this.sessionId = sessionId;
    }

    public Packet(Command cmd) {
        this.cmd = cmd.cmd;
    }

    public Packet(Command cmd, int sessionId) {
        this.cmd = cmd.cmd;
        this.sessionId = sessionId;
    }

    public int getBodyLength() {
        return body == null ? 0 : body.length;
    }

    public void addFlag(byte flag) {
        this.flags |= flag;
    }

    public boolean hasFlag(byte flag) {
        return (flags & flag) != 0;
    }

    public short calcCheckCode() {
        short checkCode = 0;
        if (body != null) {
            for (int i = 0; i < body.length; i++) {
                checkCode += (body[i] & 0x0ff);
            }
        }
        return checkCode;
    }

    public byte calcLrc() {
        byte[] data = Unpooled.buffer(HEADER_LEN - 1)
                .writeInt(getBodyLength())
                .writeByte(cmd)
                .writeShort(cc)
                .writeByte(flags)
                .writeInt(sessionId)
                .array();
        byte lrc = 0;
        for (int i = 0; i < data.length; i++) {
            lrc ^= data[i];
        }
        return lrc;
    }

    public boolean vaildCheckCode() {
        return calcCheckCode() == cc;
    }

    public boolean validLrc() {
        return (lrc ^ calcLrc()) == 0;
    }

    public InetSocketAddress sender() {
        return null;
    }

    public void setRecipient(InetSocketAddress sender) {
    }

    public Packet response(Command command) {
        return new Packet(command, sessionId);
    }

    @Override
    public String toString() {
        return "{" +
                "cmd=" + cmd +
                ", cc=" + cc +
                ", flags=" + flags +
                ", sessionId=" + sessionId +
                ", lrc=" + lrc +
                ", body=" + (body == null ? 0 : body.length) +
                '}';
    }

    public static ByteBuf getHBPacket() {
        return Unpooled.wrappedBuffer(HB_PACKET_BYTES);
    }
}
