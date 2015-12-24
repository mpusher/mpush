package com.shinemo.mpush.api;

import io.netty.channel.Channel;

import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Connection {

    String getId();

    void send(Packet packet);

    boolean isClosed();

    boolean isOpen();
    
    int getHbTimes();
    
    void close();

	boolean isConnected();

	boolean isEnable();

	void init(Channel channel);
	
	String remoteIp();
    
}
