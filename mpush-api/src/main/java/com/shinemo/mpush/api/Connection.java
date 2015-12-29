package com.shinemo.mpush.api;

import io.netty.channel.Channel;

import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Connection {

    void init(Channel channel);

    SessionContext getSessionContext();

    void setSessionContext(SessionContext context);

    void send(Packet packet);

    Channel channel();

    String getId();

    boolean isClosed();

    boolean isOpen();

    int getHbTimes();

    void close();

    boolean isConnected();

    boolean isEnable();

    String remoteIp();


}
