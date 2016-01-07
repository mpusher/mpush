package com.shinemo.mpush.netty.client;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.Client;

public class NettyClientFactory extends AbstractNettyClientFactory {

    public static final NettyClientFactory INSTANCE = new NettyClientFactory();

    Client createClient(String host, int port, ChannelHandler handler) {
        return new NettyClient(host, port, handler);
    }
}
