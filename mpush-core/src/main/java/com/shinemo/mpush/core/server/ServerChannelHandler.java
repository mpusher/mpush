package com.shinemo.mpush.core.server;


import com.shinemo.mpush.api.connection.ConnectionManager;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.netty.connection.NettyConnection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChannelHandler.class);

    private final ConnectionManager connectionManager;
    private final PacketReceiver receiver;
    private boolean security = true;

    public ServerChannelHandler(ConnectionManager connectionManager, PacketReceiver receiver) {
        this.connectionManager = connectionManager;
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Connection connection = connectionManager.get(ctx.channel());
        receiver.onReceive((Packet) msg, connection);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connectionManager.remove(ctx.channel());
        LOGGER.error(ctx.channel().remoteAddress() + ", exceptionCaught", cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn(ctx.channel().remoteAddress() + ",  channelActive");
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn(ctx.channel().remoteAddress() + ",  channelInactive");
        connectionManager.remove(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            switch (stateEvent.state()) {
                case READER_IDLE:
                    connectionManager.remove(ctx.channel());
                    ctx.close();
                    LOGGER.warn("heartbeat read timeout, chanel closed!");
                    break;
                case WRITER_IDLE:
                    ctx.writeAndFlush(Packet.getHBPacket());
                    LOGGER.warn("heartbeat write timeout, do write an EOL.");
                    break;
                case ALL_IDLE:
            }
        } else {
            LOGGER.warn("One user event Triggered. evt=" + evt);
            super.userEventTriggered(ctx, evt);
        }
    }

    public ServerChannelHandler setSecurity(boolean security) {
        this.security = security;
        return this;
    }
}