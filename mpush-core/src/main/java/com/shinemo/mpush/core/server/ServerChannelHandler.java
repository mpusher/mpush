package com.shinemo.mpush.core.server;


import com.shinemo.mpush.api.connection.ConnectionManager;
import com.shinemo.mpush.api.event.ConnectionCloseEvent;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.common.EventBus;
import com.shinemo.mpush.common.Profiler;
import com.shinemo.mpush.log.LogLevel;
import com.shinemo.mpush.log.LoggerManage;
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
    	try{
    		 Profiler.start("end channel read:");
    		 Connection connection = connectionManager.get(ctx.channel());
	         LOGGER.debug("channelRead channel={}, packet={}", ctx.channel(), msg);
	         connection.updateLastReadTime();
	         receiver.onReceive((Packet) msg, connection);
    	}finally{
    		Profiler.release();
    		long duration = Profiler.getDuration();
    		if(duration>100){
    			LOGGER.error("end channel read:"+duration+","+Profiler.dump());
    		}
    		Profiler.reset();
    	}
       
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connectionManager.remove(ctx.channel());
        LoggerManage.log(security, LogLevel.INFO, "client exceptionCaught channel={}", ctx.channel());
        LOGGER.error("caught an ex, channel={}", ctx.channel(), cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LoggerManage.log(false, LogLevel.INFO, "client connect channel={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LoggerManage.log(false, LogLevel.INFO, "client disconnect channel={}", ctx.channel());
        Connection connection = connectionManager.get(ctx.channel());
        EventBus.INSTANCE.post(new ConnectionCloseEvent(connection));
        connectionManager.remove(ctx.channel());
    }
}