package com.shinemo.mpush.tools.thread;

import java.util.concurrent.Executor;

import com.shinemo.mpush.tools.Constants;


public class ThreadPoolUtil {
    public static final String THREAD_NAME_PREFIX = "mp-t-";
    private static final ThreadPoolManager threadPoolManager =
            new ThreadPoolManager(Constants.MIN_POOL_SIZE, Constants.MAX_POOL_SIZE, Constants.THREAD_QUEUE_SIZE);

    private static Executor bossExecutor = ThreadPoolUtil.getThreadPoolManager()
            .getThreadExecutor(ThreadNameSpace.NETTY_BOSS, Constants.MIN_BOSS_POOL_SIZE, Constants.MAX_BOSS_POLL_SIZE);
    private static Executor workExecutor = ThreadPoolUtil.getThreadPoolManager()
            .getThreadExecutor(ThreadNameSpace.NETTY_WORKER, Constants.MIN_WORK_POOL_SIZE, Constants.MAX_WORK_POOL_SIZE);

    public static ThreadPoolManager getThreadPoolManager() {
        return threadPoolManager;
    }

    public static Executor getBossExecutor() {
        return bossExecutor;
    }

    public static Executor getWorkExecutor() {
        return workExecutor;
    }


    public static Thread newThread(Runnable r, String name, boolean daemon) {
        Thread t = new Thread(r);
        t.setDaemon(daemon);
        t.setName(THREAD_NAME_PREFIX + name);
        return t;
    }

    public static Thread newThread(Runnable r, String name) {
        return newThread(r, name, true);
    }
}
