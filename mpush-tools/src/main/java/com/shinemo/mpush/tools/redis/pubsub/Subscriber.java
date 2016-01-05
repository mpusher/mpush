package com.shinemo.mpush.tools.redis.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPubSub;

public class Subscriber extends JedisPubSub{
	
	private static final Logger log = LoggerFactory.getLogger(Subscriber.class);
	
	@Override
	public void onMessage(String channel, String message) {
		log.warn("onMessage channel:"+channel+","+message);
		super.onMessage(channel, message);
	}
	
	@Override
	public void onPMessage(String pattern, String channel, String message) {
		log.warn("onPMessage:"+pattern+","+channel+","+message);
		super.onPMessage(pattern, channel, message);
	}
	
	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		log.warn("onPSubscribe:"+pattern+","+subscribedChannels);
		super.onPSubscribe(pattern, subscribedChannels);
	}
	
	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		log.warn("onPUnsubscribe:"+pattern+","+subscribedChannels);
		super.onPUnsubscribe(pattern, subscribedChannels);
	}
	
	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		log.warn("onSubscribe:"+channel+","+subscribedChannels);
		super.onSubscribe(channel, subscribedChannels);
	}
	
	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {
		log.warn("onUnsubscribe:"+channel+","+subscribedChannels);
		super.onUnsubscribe(channel, subscribedChannels);
	}
	
	
	@Override
	public void unsubscribe() {
		log.warn("unsubscribe:");
		super.unsubscribe();
	}
	
	@Override
	public void unsubscribe(String... channels) {
		log.warn("unsubscribe:"+channels);
		super.unsubscribe(channels);
	}
	
	
}
