package com.mpush.tools.thread;

public final class ThreadNames {
    public static final String NS = "mp";
    public static final String THREAD_NAME_PREFIX = NS + "-t-";

    /**
     * netty boss 线程
     */
    public static final String NETTY_BOSS = NS + "-boss-";

    /**
     * netty worker 线程
     */
    public static final String NETTY_WORKER = NS + "-worker-";

    public static final String NETTY_HTTP = NS + "-http-";

    public static final String EVENT_BUS = NS + "-event-";

    public static final String REDIS = NS + "-redis-";

    public static final String ZK = NS + "-zk-";

    public static final String BIZ = NS + "-biz-";

    /**
     * connection 定期检测线程
     */
    public static final String NETTY_TIMER = NS + "-timer-";

}
