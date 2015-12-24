package com.shinemo.mpush.connection.netty.handler;


import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.core.ConnectionManager;
import com.shinemo.mpush.core.MessageReceiver;
import com.shinemo.mpush.core.NettyConnection;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * Created by ohun on 2015/12/19.
 */
public class ConnectionHandler extends ChannelHandlerAdapter {
    private MessageReceiver receiver = new MessageReceiver();
    private Connection connection = new NettyConnection();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        receiver.onMessage(new Request((Packet) msg, connection));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ConnectionManager.INSTANCE.remove(connection);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, promise);
        connection.init(ctx.channel());
        ConnectionManager.INSTANCE.add(connection);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
        ConnectionManager.INSTANCE.remove(connection);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
        ConnectionManager.INSTANCE.remove(connection);
    }
}
