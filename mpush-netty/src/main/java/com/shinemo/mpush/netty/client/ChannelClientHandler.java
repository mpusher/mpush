package com.shinemo.mpush.netty.client;

import com.shinemo.mpush.api.Client;

import io.netty.channel.ChannelHandler;

public interface ChannelClientHandler extends ChannelHandler{
	
	public Client getClient();

}
