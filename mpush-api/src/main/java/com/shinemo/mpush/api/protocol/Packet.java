package com.shinemo.mpush.api.protocol;

import com.shinemo.mpush.api.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.Serializable;

import java.util.Arrays;

/**
 * Created by ohun on 2015/12/19.
 * length(4)+cmd(1)+cc(2)+flags(1)+sessionId(4)+lrc(1)+body(n)
 */
public final class Packet implements Serializable {
    public static final byte HB_PACKET = '\n';
    private static final long serialVersionUID = -2725825199998223372L;
    public byte cmd; //命令
    public short cc; //校验码 暂时没有用到
    public byte flags; //特性，如是否加密，是否压缩等
    public int sessionId; // 会话id。客户端生成。
    public byte lrc; // 校验，纵向冗余校验。只校验body
    public byte[] body;

    public Packet(byte cmd) {
        this.cmd = cmd;
    }

    public Packet(byte cmd, int sessionId) {
        this.cmd = cmd;
        this.sessionId = sessionId;
    }

    public int getBodyLength() {
        return body == null ? 0 : body.length;
    }

    public String getStringBody() {
        return body == null ? "" : new String(body, Constants.UTF_8);
    }

    public void setFlag(byte flag) {
        this.flags |= flag;
    }

    public boolean hasFlag(byte flag) {
        return (flags & flag) != 0;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "cmd=" + cmd +
                ", cc=" + cc +
                ", flags=" + flags +
                ", sessionId=" + sessionId +
                ", lrc=" + lrc +
                ", body=" + Arrays.toString(body) +
                '}';
    }

    public static ByteBuf getHBPacket() {
        return Unpooled.buffer(1).writeByte(HB_PACKET);
    }
}
