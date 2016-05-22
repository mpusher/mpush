package com.mpush.client.connect;

import com.mpush.netty.client.NettyClient;
import io.netty.channel.ChannelHandler;

public class ConnectClient extends NettyClient {

    private final ConnClientChannelHandler handler;

    public ConnectClient(String host, int port, ClientConfig config) {
        super(host, port);
        handler = new ConnClientChannelHandler(config);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return handler;
    }

}
