package com.shinemo.mpush.netty.client;

import io.netty.channel.*;

import com.shinemo.mpush.api.Client;

public class NettyClientFactory extends AbstractNettyClientFactory {

    public static final NettyClientFactory INSTANCE = new NettyClientFactory();

    public Client createClient(String host, int port, ChannelHandler handler) {
        return new NettyClient(host, port, handler);
    }
}
