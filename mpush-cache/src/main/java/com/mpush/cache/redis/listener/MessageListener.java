package com.mpush.cache.redis.listener;


public interface MessageListener {
	
	void onMessage(String channel, String message);

}
