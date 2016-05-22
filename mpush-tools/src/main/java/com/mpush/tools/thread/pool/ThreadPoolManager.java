package com.mpush.tools.thread.pool;

import com.mpush.api.spi.SpiLoader;
import com.mpush.api.spi.common.ThreadPoolFactory;
import com.mpush.tools.thread.NamedThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class ThreadPoolManager {
    public static final ThreadPoolManager I = new ThreadPoolManager();

    private final ThreadPoolFactory threadPoolFactory = SpiLoader.load(ThreadPoolFactory.class);
    private final NamedThreadFactory threadFactory = new NamedThreadFactory();

    private Executor bossExecutor;
    private Executor workExecutor;
    private Executor bizExecutor;
    private Executor eventBusExecutor;
    private Executor redisExecutor;
    private Executor httpExecutor;

    public final Thread newThread(String name, Runnable target) {
        return threadFactory.newThread(name, target);
    }

    public Executor getHttpExecutor() {
        if (httpExecutor == null) {
            synchronized (this) {
                httpExecutor = threadPoolFactory.get(ThreadPoolFactory.HTTP_CLIENT_WORK);
            }
        }
        return httpExecutor;
    }

    public Executor getRedisExecutor() {
        if (redisExecutor == null) {
            synchronized (this) {
                redisExecutor = threadPoolFactory.get(ThreadPoolFactory.MQ);
            }
        }
        return redisExecutor;
    }

    public Executor getEventBusExecutor() {
        if (eventBusExecutor == null) {
            synchronized (this) {
                eventBusExecutor = threadPoolFactory.get(ThreadPoolFactory.EVENT_BUS);
            }
        }
        return eventBusExecutor;
    }

    public Executor getBizExecutor() {
        if (bizExecutor == null) {
            synchronized (this) {
                bizExecutor = threadPoolFactory.get(ThreadPoolFactory.BIZ);
            }
        }
        return bizExecutor;
    }

    public Executor getWorkExecutor() {
        if (workExecutor == null) {
            synchronized (this) {
                workExecutor = threadPoolFactory.get(ThreadPoolFactory.SERVER_WORK);
            }
        }
        return workExecutor;
    }

    public Executor getBossExecutor() {
        if (bossExecutor == null) {
            synchronized (this) {
                bossExecutor = threadPoolFactory.get(ThreadPoolFactory.SERVER_BOSS);
            }
        }
        return bossExecutor;
    }

    public Map<String, Executor> getActivePools() {
        Map<String, Executor> map = new HashMap<>();
        if (bossExecutor != null) map.put("bossExecutor", bossExecutor);
        if (workExecutor != null) map.put("workExecutor", workExecutor);
        if (bizExecutor != null) map.put("bizExecutor", bizExecutor);
        if (eventBusExecutor != null) map.put("eventBusExecutor", eventBusExecutor);
        if (redisExecutor != null) map.put("redisExecutor", redisExecutor);
        if (httpExecutor != null) map.put("httpExecutor", httpExecutor);
        return map;
    }

}
