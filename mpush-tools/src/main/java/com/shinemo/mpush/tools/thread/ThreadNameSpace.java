package com.shinemo.mpush.tools.thread;

public class ThreadNameSpace {

    /**
     * netty boss 线程
     */
    public static final String NETTY_BOSS = "mp-boss";

    /**
     * netty worker 线程
     */
    public static final String NETTY_WORKER = "mp-worker";

    /**
     * connection 定期检测线程
     */
    public static final String NETTY_TIMER = "mp-timer";

    public static final String getUniqueName(String serviceName) {
        return "mp-sn-" + serviceName;
    }

}
