package com.shinemo.mpush.client;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushCallbackBus implements Runnable {
    public static final PushCallbackBus INSTANCE = new PushCallbackBus();
    private Map<Integer, PushCallback> callbacks = new ConcurrentHashMap<>();
    private Executor executor = Executors.newFixedThreadPool(5);//test
    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();//test

    public PushCallbackBus() {
        scheduledExecutor.scheduleAtFixedRate(this, 1, 3, TimeUnit.SECONDS);
    }

    public void register(int reqId, PushCallback callback) {
        callbacks.put(reqId, callback);
    }

    public PushCallback get(int reqId) {
        return callbacks.get(reqId);
    }

    public Executor getExecutor() {
        return executor;
    }

    @Override
    public void run() {
        if (callbacks.isEmpty()) return;
        for (PushCallback callback : callbacks.values()) {
            if (callback.isTimeout()) {
                callback.timeout();
            }
        }
    }
}
