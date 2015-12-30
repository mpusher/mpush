package com.shinemo.mpush.api;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
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
