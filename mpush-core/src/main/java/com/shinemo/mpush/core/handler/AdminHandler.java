package com.shinemo.mpush.core.handler;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.RedisKey;
import com.shinemo.mpush.conn.client.ConnectionServerApplication;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import com.shinemo.mpush.tools.spi.ServiceContainer;
import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.ZkRegister;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public final class AdminHandler extends SimpleChannelInboundHandler<String> {
	
	private static final Logger log = LoggerFactory.getLogger(AdminHandler.class);

	private static final String DOUBLE_END = "\r\n\r\n";
	
	private static final String ONE_END = "\r\n";
	
	protected static final ZkRegister zkRegister = ServiceContainer.getInstance(ZkRegister.class);
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String request) throws Exception {
		Command command = Command.getCommand(request);
		ChannelFuture future = ctx.write(command.handler(request)+DOUBLE_END);
		if(command.equals(Command.QUIT)){
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.write("welcome to "+MPushUtil.getInetAddress()+ "!"+ONE_END);
		ctx.write("It is " + new Date() + " now."+DOUBLE_END);
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
	            buf.append("Command:"+ONE_END);
	            buf.append("help:display all command."+ONE_END);
	            buf.append("quit:exit telnet."+ONE_END);
	            buf.append("scn:statistics conn num."+ONE_END);
	            buf.append("rcs:remove connection server zk info."+ONE_END);
	            buf.append("scs:stop connection server.");
	            return buf.toString();
			}
		},
		QUIT("quit"){
			@Override
			public String handler(String request) {
				return "have a good day!";
			}
		},
		SCN("scn"){
			@Override
			public String handler(String request) {
				Long value = RedisManage.zCard(RedisKey.getUserOnlineKey(MPushUtil.getExtranetAddress()));
				if(value == null){
					value = 0L;
				}
				return value.toString()+".";
			}
		},
		RCS("rcs"){
			@Override
			public String handler(String request) {
				
				List<String> rawData = zkRegister.getChildrenKeys(ZKPath.CONNECTION_SERVER.getPath());
				boolean removeSuccess = false;
				for (String raw : rawData) {
					String data = zkRegister.get(ZKPath.CONNECTION_SERVER.getFullPath(raw));
					ConnectionServerApplication application = Jsons.fromJson(data, ConnectionServerApplication.class);
					if(application.getIp().equals(MPushUtil.getInetAddress())){
						zkRegister.remove(ZKPath.CONNECTION_SERVER.getFullPath(raw));
						log.info("delete connection server success:{}",data);
						removeSuccess = true;
					}else{
						log.info("delete connection server failed: required ip:{}, but:{}",application.getIp(),MPushUtil.getInetAddress());
					}
				}
				if(removeSuccess){
					return "remove success.";
				}else{
					return "remove false.";
				}
			}
		},
		SCS("scs"){
			@Override
			public String handler(String request) {
				return "not support now.";
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
