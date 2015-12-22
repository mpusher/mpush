package com.shinemo.mpush.api.protocol;

/**
 * Created by ohun on 2015/12/19.
 * +-----------+-------+-----+---------+--------------+------+
 * |msgic num 2| cmd 1| id 4| flags 1 | data length 4| body n
 * +-----------+------+-----+---------+--------------+------+
 */
public class Packet {
    public byte command;
    public int version;
    public byte flags;
    public int msgId;
    public int msgType;
    public byte[] body;
}
