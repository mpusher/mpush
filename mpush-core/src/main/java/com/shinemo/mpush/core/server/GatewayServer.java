package com.shinemo.mpush.core.server;

import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.common.MessageDispatcher;
import com.shinemo.mpush.core.handler.*;
import com.shinemo.mpush.netty.connection.NettyConnectionManager;
import com.shinemo.mpush.netty.server.NettyServer;
import io.netty.channel.ChannelHandler;

/**
 * Created by ohun on 2015/12/30.
 */
public final class GatewayServer extends NettyServer {

    private ServerChannelHandler channelHandler;

    public GatewayServer(int port) {
        super(port);
    }

    @Override
    public void init() {
        MessageDispatcher receiver = new MessageDispatcher();
        receiver.register(Command.GATEWAY_PUSH, new GatewayPushHandler());
        NettyConnectionManager connectionManager = new NettyConnectionManager();
        channelHandler = new ServerChannelHandler(connectionManager, receiver);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }
}
