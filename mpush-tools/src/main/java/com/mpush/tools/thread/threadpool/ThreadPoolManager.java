package com.mpush.tools.thread.threadpool;

import com.mpush.tools.thread.threadpool.cached.CachedThreadPool;
import com.mpush.tools.thread.threadpool.fixed.FixedThreadPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class ThreadPoolManager {

    private static final Map<String, Executor> poolCache = new HashMap<String, Executor>();

    private static ThreadPool cachedThreadPool = new CachedThreadPool();

    private static ThreadPool fixedThreadPool = new FixedThreadPool();

    public static Executor bossExecutor = cachedThreadPool.getExecutor(ThreadPoolContext.BOSS_THREAD_POOL);
    public static Executor workExecutor = cachedThreadPool.getExecutor(ThreadPoolContext.WORK_THREAD_POOL);
    public static Executor bizExecutor = fixedThreadPool.getExecutor(ThreadPoolContext.BIZ_THREAD_POOL);
    public static Executor eventBusExecutor = fixedThreadPool.getExecutor(ThreadPoolContext.EVENT_BUS_THREAD_POOL);
    public static Executor redisExecutor = fixedThreadPool.getExecutor(ThreadPoolContext.REDIS_THREAD_POOL);

    public static Executor httpExecutor = fixedThreadPool.getExecutor(ThreadPoolContext.HTTP_THREAD_POOL);

    static {
        poolCache.put(ThreadPoolContext.BOSS_THREAD_POOL.getName(), bossExecutor);
        poolCache.put(ThreadPoolContext.WORK_THREAD_POOL.getName(), workExecutor);
        poolCache.put(ThreadPoolContext.BIZ_THREAD_POOL.getName(), bizExecutor);
        poolCache.put(ThreadPoolContext.EVENT_BUS_THREAD_POOL.getName(), eventBusExecutor);
        poolCache.put(ThreadPoolContext.REDIS_THREAD_POOL.getName(), redisExecutor);
        poolCache.put(ThreadPoolContext.HTTP_THREAD_POOL.getName(), httpExecutor);
    }

    public static final Map<String, Executor> getPool() {
        return poolCache;
    }

    public static final Thread newThread(String name, Runnable target) {
        Thread thread = new Thread(target, name);
        return thread;
    }


}
