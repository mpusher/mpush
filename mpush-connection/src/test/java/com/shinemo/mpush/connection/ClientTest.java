package com.shinemo.mpush.connection;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.connection.netty.NettySharedHolder;
import com.shinemo.mpush.connection.netty.codec.PacketDecoder;
import com.shinemo.mpush.connection.netty.codec.PacketEncoder;
import com.shinemo.mpush.core.ConnectionManager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * Created by ohun on 2015/12/24.
 */
public class ClientTest {
	
	private static final Logger log = LoggerFactory.getLogger(ClientTest.class);

	@Test
	public void testClient() {
		String host = "127.0.0.1";
		int port = 3000;
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap(); // (1)
			b.group(workerGroup); // (2)
			b.channel(NioSocketChannel.class); // (3)
			b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new PacketDecoder());
					ch.pipeline().addLast(PacketEncoder.INSTANCE);
					ch.pipeline().addLast(new ClientHandler());
				}
			});
			ChannelFuture future = b.connect(host, port).sync(); // (5)
			future.channel().closeFuture().sync();
			if (future.awaitUninterruptibly(4000) && future.isSuccess()) {
				final Channel channel = future.channel();

				startHeartBeat(channel);
			} else {
				future.cancel(true);
				future.channel().close();
			}
			
			log.error("for test");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
		}

	}
	
	public void startHeartBeat(final Channel channel){
		NettySharedHolder.timer.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				try{
					final Packet packet = buildHeartBeat();
					ChannelFuture channelFuture = channel.writeAndFlush(packet);
					channelFuture.addListener(new ChannelFutureListener() {
						
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							if(!future.isSuccess()){
								if(!channel.isActive()){
									log.warn("client send msg false:"+channel.remoteAddress().toString()+","+packet+",channel is not active");
									ConnectionManager.INSTANCE.remove(channel);
								}
								log.warn("client send msg false:"+channel.remoteAddress().toString()+","+packet);
							}else{
								log.warn("client send msg success:"+channel.remoteAddress().toString()+","+packet);
							}
						}
					});
				}finally{
					if(channel.isActive()){
						NettySharedHolder.timer.newTimeout(this, Constants.TIME_DELAY, TimeUnit.SECONDS);
					}
				}
			}
		}, Constants.TIME_DELAY, TimeUnit.SECONDS);
	}
	
	private static Packet buildHeartBeat() {
		Packet packet = new Packet();
		packet.command = Command.Heartbeat.cmd;
		packet.version = 0;
		packet.flags = 0;
		packet.msgId = 1;
		return packet;
	}



}
