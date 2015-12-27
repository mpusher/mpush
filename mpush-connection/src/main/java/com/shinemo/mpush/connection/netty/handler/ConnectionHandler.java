package com.shinemo.mpush.connection.netty.handler;


import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.core.ConnectionManager;
import com.shinemo.mpush.core.MessageReceiver;
import com.shinemo.mpush.core.NettyConnection;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
@ChannelHandler.Sharable
public class ConnectionHandler extends ChannelHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

    private final MessageReceiver receiver;

    public ConnectionHandler(MessageReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Connection connection = ConnectionManager.INSTANCE.get(ctx.channel());
        receiver.onMessage((Packet) msg, connection);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ConnectionManager.INSTANCE.remove(ctx.channel());
        log.error(ctx.channel().remoteAddress() + ", exceptionCaught", cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.warn(ctx.channel().remoteAddress() + ",  channelActive");
        Connection connection = new NettyConnection();
        connection.init(ctx.channel());
        ConnectionManager.INSTANCE.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn(ctx.channel().remoteAddress() + ",  channelInactive");
        ConnectionManager.INSTANCE.remove(ctx.channel());
    }
}
