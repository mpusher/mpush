package com.shinemo.mpush.api.protocol;

import com.shinemo.mpush.api.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by ohun on 2015/12/19.
 * length(4)+cmd(1)+cc(2)+flags(1)+sessionId(4)+lrc(1)+body(n)
 */
public final class Packet {
    public static final int HEADER_LEN = 13;
    public static final byte FLAG_CRYPTO = 0x01;
    public static final byte FLAG_COMPRESS = 0x02;

    public static final byte HB_PACKET_BYTE = 0;
    public static final byte[] HB_PACKET_BYTES = new byte[]{HB_PACKET_BYTE};

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

    public void setFlag(byte flag) {
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

    @Override
    public String toString() {
        return "Packet{" +
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
