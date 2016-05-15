package com.mpush.api;

import com.mpush.api.protocol.Packet;
import com.mpush.api.connection.Connection;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Message {

    Connection getConnection();

    void send(ChannelFutureListener listener);

    void sendRaw(ChannelFutureListener listener);

    Packet getPacket();
}
