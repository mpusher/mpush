package com.shinemo.mpush.api;


public interface Client {


    boolean isConnected();

	String getHost();

	int getPort();

	void close(String cause);

	boolean isEnabled();

	void resetHbTimes();

	int inceaseAndGetHbTimes();

	String getUrl();

	void startHeartBeat() throws Exception;

}
