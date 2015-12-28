package com.shinemo.mpush.netty.util;

import com.shinemo.mpush.api.protocol.Handler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettySharedHandler extends ChannelHandlerAdapter{
	
	private static final Logger log = LoggerFactory.getLogger(NettySharedHandler.class);
	
	private Handler channelHandler;
	
	public NettySharedHandler(Handler channelHandler){
		this.channelHandler = channelHandler;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.warn(ctx.channel().remoteAddress() + ", channelActive");
		if(channelHandler!=null){
			channelHandler.channelActive(ctx);
		}
		super.channelActive(ctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.warn(ctx.channel().remoteAddress() + ", channelInactive");
		if(channelHandler!=null){
			channelHandler.channelInactive(ctx);
		}
		super.channelInactive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.warn(ctx.channel().remoteAddress() + ", channelRead:"+ToStringBuilder.reflectionToString(msg, ToStringStyle.DEFAULT_STYLE));
		if(channelHandler!=null){
			channelHandler.channelRead(ctx, msg);
		}
		super.channelRead(ctx, msg);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		log.warn(ctx.channel().remoteAddress() + ", channelReadComplete");
		if(channelHandler!=null){
			channelHandler.channelReadComplete(ctx);
		}
		super.channelReadComplete(ctx);
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//		log.warn(ctx.channel().remoteAddress() + ", channelRegistered");
		if(channelHandler!=null){
			channelHandler.channelRegistered(ctx);
		}
		super.channelRegistered(ctx);
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//		log.warn(ctx.channel().remoteAddress() + ", channelUnregistered");
		if(channelHandler!=null){
			channelHandler.channelUnregistered(ctx);
		}
		super.channelUnregistered(ctx);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.warn(ctx.channel().remoteAddress() + ", exceptionCaught",cause);
		if(channelHandler!=null){
			channelHandler.exceptionCaught(ctx, cause);
		}
		super.exceptionCaught(ctx, cause);
	}
	

}
