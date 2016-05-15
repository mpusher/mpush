package com.mpush.core.server;

import com.mpush.api.protocol.Command;
import com.mpush.common.MessageDispatcher;
import com.mpush.core.handler.GatewayPushHandler;
import com.mpush.netty.connection.NettyConnectionManager;
import com.mpush.netty.server.NettyServer;
import io.netty.channel.ChannelHandler;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public final class GatewayServer extends NettyServer {

    private ServerChannelHandler channelHandler;
    private NettyConnectionManager connectionManager;

    public GatewayServer(int port) {
        super(port);
    }

    @Override
    public void init() {
        super.init();
        MessageDispatcher receiver = new MessageDispatcher();
        receiver.register(Command.GATEWAY_PUSH, new GatewayPushHandler());
        connectionManager = new NettyConnectionManager();
        channelHandler = new ServerChannelHandler(false, connectionManager, receiver);
    }

    @Override
    public void stop(Listener listener) {
        super.stop(listener);
        connectionManager.destroy();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }
}
