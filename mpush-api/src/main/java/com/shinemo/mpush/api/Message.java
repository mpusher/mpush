package com.shinemo.mpush.api;

import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Message {

    Connection getConnection();

    void send();

    void sendRaw();

    Packet getPacket();
}
