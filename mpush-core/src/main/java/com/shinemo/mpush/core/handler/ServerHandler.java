package com.shinemo.mpush.core.handler;


import java.net.SocketAddress;

import com.shinemo.mpush.api.protocol.Handler;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.core.ConnectionManager;
import com.shinemo.mpush.core.MessageReceiver;
import com.shinemo.mpush.core.NettyConnection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
public class ServerHandler implements Handler {

    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);

    private final MessageReceiver receiver;

    public ServerHandler(MessageReceiver receiver) {
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

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
		
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		
	}
}