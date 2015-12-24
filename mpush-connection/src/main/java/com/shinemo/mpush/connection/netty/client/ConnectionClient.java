package com.shinemo.mpush.connection.netty.client;

import io.netty.channel.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionClient {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionClient.class);
	
	private final Channel channel;
	private int hbTimes = 0;
	
	public ConnectionClient(final String remoteIp,final int remotePort,final Channel channel) {
		this.channel = channel;
	}

}
