package com.shinemo.mpush.api.protocol;

/**
 * Created by ohun on 2015/12/19.
 * magic(2)+cmd(1)+version(1)+flags(1)+msgId(4)+length(4)+body(n)
 */
public class Packet {
    public byte command;
    public byte version;
    public byte flags;
    public int msgId;
    public byte[] body;

    public int getBodyLength() {
        return body == null ? 0 : body.length;
    }
}
