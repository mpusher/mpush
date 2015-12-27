package com.shinemo.mpush.api.protocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.List;

import com.shinemo.mpush.api.Connection;

public interface Handler {
	
	public void channelActive(ChannelHandlerContext ctx) throws Exception;
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception;
	
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
	
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception;
	
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception;
	
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception;
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
	
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception;
	
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;
	
}
