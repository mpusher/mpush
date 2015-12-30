package com.shinemo.mpush.api;

import io.netty.channel.ChannelHandler;

public interface Client {

    void init();

    void start();

    void stop();

    boolean isConnected();

    String getUri();

    ChannelHandler getHandler();

}
