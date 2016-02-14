package com.shinemo.mpush.api;

import io.netty.channel.Channel;

import com.shinemo.mpush.api.connection.Connection;


public interface Client {

	void init(Channel channel);

    boolean isConnected();

	String getHost();

	int getPort();

	void close(String cause);

	boolean isEnabled();

	void resetHbTimes();

	int inceaseAndGetHbTimes();

	String getUrl();

	void startHeartBeat() throws Exception;
	
	void startHeartBeat(final int heartbeat) throws Exception;
	
	void stop();
	
	Connection getConnection();

	Channel getChannel();

	void initConnection(Connection connection);

}
