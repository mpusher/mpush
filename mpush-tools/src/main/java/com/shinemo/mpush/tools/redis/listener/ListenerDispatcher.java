package com.shinemo.mpush.tools.redis.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.common.collect.Maps;

public class ListenerDispatcher implements MessageListener {

    public static final ListenerDispatcher INSTANCE = new ListenerDispatcher();

    private Map<String, List<MessageListener>> subscribes = Maps.newTreeMap();

    private Executor executor = Executors.newFixedThreadPool(5);

    @Override
    public void onMessage(final String channel, final String message) {
        List<MessageListener> listeners = subscribes.get(channel);
        if (listeners == null) {
            return;
        }
        for (final MessageListener l : listeners) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    l.onMessage(channel, message);
                }
            });
        }
    }

    public void subscribe(String channel, MessageListener l) {
        List<MessageListener> listeners = subscribes.get(channel);
        if (listeners == null) {
            listeners = new ArrayList<>();
            subscribes.put(channel, listeners);
        }
        listeners.add(l);
    }
}
