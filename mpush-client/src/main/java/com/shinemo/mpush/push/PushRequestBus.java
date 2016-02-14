package com.shinemo.mpush.push;

import java.util.Iterator;
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

    private PushRequestBus() {
        scheduledExecutor.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
    }

    public void put(int sessionId, PushRequest request) {
        requests.put(sessionId, request);
    }

    public PushRequest remove(int sessionId) {
        return requests.remove(sessionId);
    }

    public Executor getExecutor() {
        return executor;
    }

    @Override
    public void run() {
        if (requests.isEmpty()) return;
        Iterator<PushRequest> it = requests.values().iterator();
        while (it.hasNext()) {
            PushRequest request = it.next();
            if (request.isTimeout()) {
                it.remove();//清除超时的请求
                request.timeout();
            }
        }
    }
}
