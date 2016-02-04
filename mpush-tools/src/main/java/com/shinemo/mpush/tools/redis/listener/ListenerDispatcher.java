package com.shinemo.mpush.tools.redis.listener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import com.shinemo.mpush.tools.redis.pubsub.Subscriber;
import com.shinemo.mpush.tools.thread.threadpool.ThreadPoolManager;

public class ListenerDispatcher implements MessageListener {

    public static final ListenerDispatcher INSTANCE = new ListenerDispatcher();

    private Map<String, List<MessageListener>> subscribes = Maps.newTreeMap();

    private Executor executor = ThreadPoolManager.redisExecutor;

    @Override
    public void onMessage(final String channel, final String message) {
        List<MessageListener> listeners = subscribes.get(channel);
        if (listeners == null) {
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
        Subscriber subscriber = new Subscriber();
        RedisManage.subscribe(subscriber, channel);
    }
}
