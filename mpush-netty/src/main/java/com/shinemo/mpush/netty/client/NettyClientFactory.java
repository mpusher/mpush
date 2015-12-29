package com.shinemo.mpush.netty.client;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.Client;

public class NettyClientFactory extends AbstractNettyClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientFactory.class);

    public static final NettyClientFactory INSTANCE = new NettyClientFactory();

    protected Client createClient(final String host, final int port, final ChannelHandler handler) throws Exception {
        return new NettyClient(host, port, handler);
    }

    public Client getClient(final Client client) throws Exception {
        return get(client.getHost(), client.getPort());
    }

    public void remove(final Client client) {
        super.remove(client);
    }

}
