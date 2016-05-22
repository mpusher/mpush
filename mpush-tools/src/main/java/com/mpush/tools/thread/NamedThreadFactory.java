package com.mpush.tools.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mpush.tools.thread.ThreadNames.THREAD_NAME_PREFIX;

/**
 * Created by xiaoxu.yxx on 2015/7/19.
 */
public final class NamedThreadFactory implements ThreadFactory {
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    protected final String namePrefix;
    protected final ThreadGroup group;


    public NamedThreadFactory() {
        this(THREAD_NAME_PREFIX);
    }

    public NamedThreadFactory(final String namePrefix) {
        this.namePrefix = namePrefix;
        this.group = Thread.currentThread().getThreadGroup();
    }

    public Thread newThread(String name, Runnable r) {
        return new Thread(group, r, name);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = newThread(namePrefix + threadNumber.getAndIncrement(), r);
        if (t.isDaemon())
            t.setDaemon(false);
        return t;
    }
}
