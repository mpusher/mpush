package com.shinemo.mpush.core.server;


import com.shinemo.mpush.api.connection.ConnectionManager;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.common.security.CipherBox;
import com.shinemo.mpush.netty.connection.NettyConnection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
@ChannelHandler.Sharable
public final class ServerChannelHandler extends ChannelHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChannelHandler.class);

    /**
     * 是否启用加密
     */
    private final boolean security;
    private final ConnectionManager connectionManager;
    private final PacketReceiver receiver;

    public ServerChannelHandler(boolean security, ConnectionManager connectionManager, PacketReceiver receiver) {
        this.security = security;
        this.connectionManager = connectionManager;
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Connection connection = connectionManager.get(ctx.channel());
        connection.updateLastReadTime();
        receiver.onReceive((Packet) msg, connection);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connectionManager.remove(ctx.channel());
        LOGGER.error("caught an ex, channel={}", ctx.channel(), cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client connect channel={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client disconnect channel={}", ctx.channel());
        connectionManager.remove(ctx.channel());
    }
}