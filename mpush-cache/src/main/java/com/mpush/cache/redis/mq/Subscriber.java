package com.mpush.cache.redis.mq;

import com.mpush.cache.redis.listener.ListenerDispatcher;
import com.mpush.tools.log.Logs;
import com.mpush.tools.Jsons;
import redis.clients.jedis.JedisPubSub;

public class Subscriber extends JedisPubSub {

    private static ListenerDispatcher dispatcher = ListenerDispatcher.I;
    
    public static Subscriber holder = new Subscriber();
    
    private Subscriber(){}
    
    @Override
    public void onMessage(String channel, String message) {
    	Logs.REDIS.info("onMessage:{},{}", channel,message);
        dispatcher.onMessage(channel, message);
        super.onMessage(channel, message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
    	Logs.REDIS.info("onPMessage:{},{},{}",pattern,channel,message);
        super.onPMessage(pattern, channel, message);
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
    	Logs.REDIS.info("onPSubscribe:{},{}",pattern,subscribedChannels);
        super.onPSubscribe(pattern, subscribedChannels);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    	Logs.REDIS.info("onPUnsubscribe:{},{}",pattern,subscribedChannels);
        super.onPUnsubscribe(pattern, subscribedChannels);
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
    	Logs.REDIS.info("onSubscribe:{},{}",channel,subscribedChannels);
        super.onSubscribe(channel, subscribedChannels);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
    	Logs.REDIS.info("onUnsubscribe:{},{}",channel,subscribedChannels);
        super.onUnsubscribe(channel, subscribedChannels);
    }


    @Override
    public void unsubscribe() {
    	Logs.REDIS.info("unsubscribe");
        super.unsubscribe();
    }

    @Override
    public void unsubscribe(String... channels) {
    	Logs.REDIS.info("unsubscribe:{}", Jsons.toJson(channels));
        super.unsubscribe(channels);
    }

}
