package com.shinemo.mpush.api.protocol;

import com.shinemo.mpush.api.Constants;

import java.io.Serializable;

import java.util.Arrays;

/**
 * Created by ohun on 2015/12/19.
 * length(4)+cmd(1)+cc(2)+flags(1)+sessionId(4)+lrc(1)+body(n)
 */
public class Packet implements Serializable {
    private static final long serialVersionUID = -2725825199998223372L;
    public byte cmd;
    public short cc;
    public byte flags;
    public int sessionId;
    public byte lrc;
    public byte[] body;
    
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
}
