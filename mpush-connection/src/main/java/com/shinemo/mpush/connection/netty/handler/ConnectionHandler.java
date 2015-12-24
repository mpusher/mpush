package com.shinemo.mpush.connection.netty.handler;


import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Message;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.core.ConnectionManager;
import com.shinemo.mpush.core.MessageReceiver;
import com.shinemo.mpush.core.NettyConnection;
import com.shinemo.mpush.core.thread.ThreadNameSpace;
import com.shinemo.mpush.core.thread.ThreadPoolUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
public class ConnectionHandler extends ChannelHandlerAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
	
    private MessageReceiver receiver;
    
    private static Executor executor = ThreadPoolUtil.getThreadPoolManager().getThreadExecutor(ThreadNameSpace.NETTY_WORKER);
    
    public ConnectionHandler(MessageReceiver receiver) {
    	this.receiver = receiver;
	}

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	log.warn(ctx.channel().remoteAddress()+",  channelRead");
    	Connection connection = ConnectionManager.INSTANCE.get(ctx.channel());
        receiver.onMessage(new Request((Packet) msg, connection));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ConnectionManager.INSTANCE.remove(ctx.channel());
        log.warn("",ctx.channel().remoteAddress()+", exceptionCaught");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	log.warn(ctx.channel().remoteAddress()+",  channelActive");
    	super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	log.warn(ctx.channel().remoteAddress()+",  channelInactive");
    	super.channelInactive(ctx);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    	log.warn(ctx.channel().remoteAddress()+",  disconnect");
        super.disconnect(ctx, promise);
        ConnectionManager.INSTANCE.remove(ctx.channel());
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    	log.warn(ctx.channel().remoteAddress()+", close");
        super.close(ctx, promise);
        ConnectionManager.INSTANCE.remove(ctx.channel());
    }
    
}
