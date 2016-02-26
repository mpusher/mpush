package com.shinemo.mpush.core.handler;

import java.util.Date;

import com.shinemo.mpush.tools.MPushUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public final class AdminHandler extends SimpleChannelInboundHandler<String> {

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String request) throws Exception {
		String response;
		boolean close = false;
		if(request.isEmpty()){
			response = "please type something.\r\n";
		}else if("quit".equals(request.toLowerCase())){
			response = "have a good day! \r\n";
			close = true;
		}else {
			response = "did you say " + request +" ? \r\n";
		}
		ChannelFuture future = ctx.write(response);
		if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.write("welcome to "+MPushUtil.getInetAddress()+ "!\r\n");
		ctx.write("It is " + new Date() + " now.\r\n");
        ctx.flush();
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
}
