package com.shinemo.mpush.api.connection;

import io.netty.channel.Channel;

import com.shinemo.mpush.api.protocol.Packet;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Connection {

    void init(Channel channel, boolean security);

    SessionContext getSessionContext();

    void setSessionContext(SessionContext context);

    ChannelFuture send(Packet packet);

    void send(Packet packet, ChannelFutureListener listener);

    Channel channel();

    String getId();

    void close();

    boolean isConnected();
}
