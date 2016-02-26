package com.shinemo.mpush.core.handler;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.shinemo.mpush.api.RedisKey;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public final class AdminHandler extends SimpleChannelInboundHandler<String> {

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String request) throws Exception {
		Command command = Command.getCommand(request);
		ChannelFuture future = ctx.write(command.handler(request));
		if(command.equals(Command.QUIT)){
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
	
	public static enum Command{
		HELP("help"){
			@Override
			public String handler(String request) {
				StringBuilder buf = new StringBuilder();
	            buf.append("Command:\r\n");
	            buf.append("help:display all command.\r\n");
	            buf.append("quit:exit telnet.\r\n");
	            buf.append("r1:statistics conn num.\r\n");
	            buf.append("r2:remove connection server zk info.\r\n");
	            buf.append("r3:stop connection server.\r\n");
	            return buf.toString();
			}
		},
		QUIT("quit"){
			@Override
			public String handler(String request) {
				return "have a good day! \r\n";
			}
		},
		R1("r1"){
			@Override
			public String handler(String request) {
				Long value = RedisManage.get(RedisKey.getConnNum(MPushUtil.getExtranetAddress()), Long.class);
				if(value == null){
					value = 0L;
				}
				return value.toString()+".\r\n";
			}
		},
		R2("r2"){
			@Override
			public String handler(String request) {
				return "not support now.\r\n";
			}
		},
		R3("r3"){
			@Override
			public String handler(String request) {
				return "not support now.\r\n";
			}
		};
		private final String cmd;
		public abstract String handler(String request);
		private Command(String cmd) {
			this.cmd = cmd;
		}
		public String getCmd() {
			return cmd;
		}
		
		public static Command getCommand(String request){
			if(StringUtils.isNoneEmpty(request)){
				for(Command command: Command.values()){
					if(command.getCmd().equals(request)){
						return command;
					}
				}
			}
			return HELP;
		}
	}
	
}
