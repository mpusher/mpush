package com.mpush.client.gateway;

import com.mpush.api.connection.Connection;
import com.mpush.netty.client.NettyClient;
import io.netty.channel.ChannelHandler;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class GatewayClient extends NettyClient {
    private final GatewayClientChannelHandler handler = new GatewayClientChannelHandler();

    public GatewayClient(String host, int port) {
        super(host, port);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return handler;
    }

    public Connection getConnection() {
        return handler.getConnection();
    }
}
