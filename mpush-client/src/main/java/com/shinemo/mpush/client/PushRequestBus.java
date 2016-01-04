package com.shinemo.mpush.client;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushRequestBus implements Runnable {
    public static final PushRequestBus INSTANCE = new PushRequestBus();
    private Map<Integer, PushRequest> requests = new ConcurrentHashMap<>();
    private Executor executor = Executors.newFixedThreadPool(5);//test
    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();//test

    public PushRequestBus() {
        scheduledExecutor.scheduleAtFixedRate(this, 1, 3, TimeUnit.SECONDS);
    }

    public void add(PushRequest request) {
        requests.put(request.getSessionId(), request);
    }

    public PushRequest remove(int reqId) {
        return requests.remove(reqId);
    }

    public Executor getExecutor() {
        return executor;
    }

    @Override
    public void run() {
        if (requests.isEmpty()) return;
        for (PushRequest callback : requests.values()) {
            if (callback.isTimeout()) {
                callback.timeout();
            }
        }
    }
}
