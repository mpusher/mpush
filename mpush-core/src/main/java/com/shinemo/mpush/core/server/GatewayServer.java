package com.shinemo.mpush.core.server;

import com.shinemo.mpush.netty.server.NettyServer;
import io.netty.channel.ChannelHandler;

/**
 * Created by ohun on 2015/12/30.
 */
public class GatewayServer extends NettyServer {

    public GatewayServer(int port, ChannelHandler channelHandler) {
        super(port, channelHandler);
    }

}
