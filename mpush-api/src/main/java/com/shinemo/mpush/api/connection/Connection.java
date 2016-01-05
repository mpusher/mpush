package com.shinemo.mpush.api.connection;

import io.netty.channel.Channel;

import com.shinemo.mpush.api.protocol.Packet;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Connection {
    int STATUS_NEW = 0;
    int STATUS_CONNECTED = 1;
    int STATUS_DISCONNECTED = 2;
    int STATUS_TIMEOUT = 3;

    void init(Channel channel, boolean security);

    SessionContext getSessionContext();

    void setSessionContext(SessionContext context);

    ChannelFuture send(Packet packet);

    ChannelFuture send(Packet packet, ChannelFutureListener listener);

    Channel channel();

    String getId();

    ChannelFuture close();

    boolean isConnected();

    boolean heartbeatTimeout();

    void updateLastReadTime();
}
