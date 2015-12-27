package com.shinemo.mpush.netty.client;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.netty.util.NettySharedHolder;

public class NettyClient implements Client {

	private static final Logger log = LoggerFactory.getLogger(NettyClient.class);

	private final String remoteHost;
	private final int remotePort;
	private final Channel channel;
	private int hbTimes = 0;

	public NettyClient(final String remoteHost, final int remotePort, Channel channel) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.channel = channel;
	}

	@Override
	public void close(String cause) {
		if (!StringUtils.isBlank(cause) && !"null".equals(cause.trim())) {
			log.error("close channel:"+cause);
		}
		this.channel.close();
	}

	@Override
	public boolean isEnabled() {
		return channel.isWritable();
	}

	@Override
	public boolean isConnected() {
		return channel.isActive();
	}

	@Override
	public void resetHbTimes() {
		hbTimes = 0;
	}

	@Override
	public int inceaseAndGetHbTimes() {
		return ++hbTimes;
	}

	@Override
	public void startHeartBeat() throws Exception {
		NettySharedHolder.timer.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				try {
					final Packet packet = buildHeartBeat();
					ChannelFuture channelFuture = channel.writeAndFlush(packet);
					channelFuture.addListener(new ChannelFutureListener() {

						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							if (!future.isSuccess()) {
								if (!channel.isActive()) {
									log.warn("client send hb msg false:" + channel.remoteAddress().toString() + "," + packet + ",channel is not active");
								}
								log.warn("client send msg hb false:" + channel.remoteAddress().toString() + "," + packet);
							} else {
								log.warn("client send msg hb success:" + channel.remoteAddress().toString() + "," + packet);
							}
						}
					});
				} finally {
					if (channel.isActive()) {
						NettySharedHolder.timer.newTimeout(this, Constants.TIME_DELAY, TimeUnit.SECONDS);
					}
				}
			}
		}, Constants.TIME_DELAY, TimeUnit.SECONDS);
	}

	private static Packet buildHeartBeat() {
		Packet packet = new Packet();
		packet.cmd = Command.Heartbeat.cmd;
		return packet;
	}

	@Override
	public String getUrl() {
		return String.format("%s:%s", remoteHost, remotePort);
	}

	@Override
	public String getRemoteHost() {
		return remoteHost;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}
}
