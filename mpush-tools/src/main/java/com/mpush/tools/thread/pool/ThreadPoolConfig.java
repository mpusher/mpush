/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.tools.thread.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public final class ThreadPoolConfig {
    public static final int REJECTED_POLICY_ABORT = 0;
    public static final int REJECTED_POLICY_DISCARD = 1;
    public static final int REJECTED_POLICY_CALLER_RUNS = 2;
    private String name;//名字
    private int corePoolSize; //最小线程大小
    private int maxPoolSize; //最大线程大小
    private int queueCapacity;  // 允许缓冲在队列中的任务数 (0:不缓冲、负数：无限大、正数：缓冲的任务数)
    private int keepAliveSeconds;// 存活时间
    private int rejectedPolicy = REJECTED_POLICY_ABORT;

    public ThreadPoolConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ThreadPoolConfig setName(String name) {
        this.name = name;
        return this;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public ThreadPoolConfig setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public ThreadPoolConfig setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public ThreadPoolConfig setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public ThreadPoolConfig setKeepAliveSeconds(long keepAliveSeconds) {
        this.keepAliveSeconds = (int) keepAliveSeconds;
        return this;
    }

    public int getRejectedPolicy() {
        return rejectedPolicy;
    }

    public ThreadPoolConfig setRejectedPolicy(int rejectedPolicy) {
        this.rejectedPolicy = rejectedPolicy;
        return this;
    }

    public static ThreadPoolConfig buildFixed(String name, int threads, int queueCapacity) {
        return new ThreadPoolConfig(name)
                .setCorePoolSize(threads)
                .setMaxPoolSize(threads)
                .setQueueCapacity(queueCapacity)
                .setKeepAliveSeconds(0);
    }

    public static ThreadPoolConfig buildCached(String name) {
        return new ThreadPoolConfig(name)
                .setKeepAliveSeconds(0);
    }

    public static ThreadPoolConfig build(String name) {
        return new ThreadPoolConfig(name);
    }


    public BlockingQueue<Runnable> getQueue() {
        BlockingQueue<Runnable> blockingQueue;
        if (queueCapacity == 0) {
            blockingQueue = new SynchronousQueue<>();
        } else if (queueCapacity < 0) {
            blockingQueue = new LinkedBlockingQueue<>();
        } else {
            blockingQueue = new LinkedBlockingQueue<>(queueCapacity);
        }
        return blockingQueue;
    }

    @Override
    public String toString() {
        return "ThreadPoolConfig{" +
                "name='" + name + '\'' +
                ", corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", queueCapacity=" + queueCapacity +
                ", keepAliveSeconds=" + keepAliveSeconds +
                '}';
    }
}
