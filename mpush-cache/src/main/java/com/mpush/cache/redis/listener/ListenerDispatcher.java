package com.mpush.cache.redis.listener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.tools.log.Logs;

import com.mpush.cache.redis.mq.Subscriber;
import com.mpush.tools.thread.pool.ThreadPoolManager;

public class ListenerDispatcher implements MessageListener {

    public static final ListenerDispatcher I = new ListenerDispatcher();

    private final Map<String, List<MessageListener>> subscribes = Maps.newTreeMap();

    private final Executor executor = ThreadPoolManager.I.getRedisExecutor();

    private ListenerDispatcher(){}

    @Override
    public void onMessage(final String channel, final String message) {
        List<MessageListener> listeners = subscribes.get(channel);
        if (listeners == null) {
        	Logs.REDIS.info("cannot find listener:%s,%s", channel,message);
            return;
        }
        for (final MessageListener listener : listeners) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                	listener.onMessage(channel, message);
                }
            });
        }
    }

    public void subscribe(String channel, MessageListener listener) {
        List<MessageListener> listeners = subscribes.get(channel);
        if (listeners == null) {
            listeners = Lists.newArrayList();
            subscribes.put(channel, listeners);
        }
        listeners.add(listener);
        RedisManager.I.subscribe(Subscriber.holder, channel);
    }
}
