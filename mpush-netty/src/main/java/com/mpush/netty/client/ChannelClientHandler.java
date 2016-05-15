package com.mpush.netty.client;

import com.mpush.api.Client;

import io.netty.channel.ChannelHandler;

public interface ChannelClientHandler extends ChannelHandler{
	
	public Client getClient();

}
