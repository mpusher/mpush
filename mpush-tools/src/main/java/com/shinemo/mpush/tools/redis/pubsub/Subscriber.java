package com.shinemo.mpush.tools.redis.pubsub;

import com.shinemo.mpush.log.LogType;
import com.shinemo.mpush.log.LoggerManage;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.redis.listener.ListenerDispatcher;

import redis.clients.jedis.JedisPubSub;

public class Subscriber extends JedisPubSub {

    private static ListenerDispatcher dispatcher = ListenerDispatcher.INSTANCE;
    
    @Override
    public void onMessage(String channel, String message) {
    	LoggerManage.log(LogType.REDIS, "onMessage:%s,%s", channel,message);
        dispatcher.onMessage(channel, message);
        super.onMessage(channel, message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
    	LoggerManage.log(LogType.REDIS, "onPMessage:%s,%s,%s",pattern,channel,message);
        super.onPMessage(pattern, channel, message);
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
    	LoggerManage.log(LogType.REDIS, "onPSubscribe:%s,%s",pattern,subscribedChannels);
        super.onPSubscribe(pattern, subscribedChannels);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    	LoggerManage.log(LogType.REDIS, "onPUnsubscribe:%s,%s",pattern,subscribedChannels);
        super.onPUnsubscribe(pattern, subscribedChannels);
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
    	LoggerManage.log(LogType.REDIS, "onSubscribe:%s,%s",channel,subscribedChannels);
        super.onSubscribe(channel, subscribedChannels);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
    	LoggerManage.log(LogType.REDIS, "onUnsubscribe:%s,%s",channel,subscribedChannels);
        super.onUnsubscribe(channel, subscribedChannels);
    }


    @Override
    public void unsubscribe() {
    	LoggerManage.log(LogType.REDIS, "unsubscribe");
        super.unsubscribe();
    }

    @Override
    public void unsubscribe(String... channels) {
    	LoggerManage.log(LogType.REDIS, "unsubscribe:%s",Jsons.toJson(channels));
        super.unsubscribe(channels);
    }

}
