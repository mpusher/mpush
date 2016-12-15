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

import com.mpush.api.spi.common.ExecutorFactory;
import com.mpush.tools.thread.NamedThreadFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ThreadProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public final class ThreadPoolManager {
    public static final ThreadPoolManager I = new ThreadPoolManager();

    private final ExecutorFactory executorFactory = ExecutorFactory.create();
    private final NamedThreadFactory threadFactory = new NamedThreadFactory();

    private final Map<String, Executor> pools = new ConcurrentHashMap<>();

    public final Thread newThread(String name, Runnable target) {
        return threadFactory.newThread(name, target);
    }

    public Executor getRedisExecutor() {
        return pools.computeIfAbsent("mq", s -> executorFactory.get(ExecutorFactory.MQ));
    }

    public Executor getEventBusExecutor() {
        return pools.computeIfAbsent("event-bus", s -> executorFactory.get(ExecutorFactory.EVENT_BUS));
    }

    public ScheduledExecutorService getPushClientTimer() {
        return (ScheduledExecutorService) pools.computeIfAbsent("push-client-timer"
                , s -> executorFactory.get(ExecutorFactory.PUSH_CLIENT));
    }

    public ScheduledExecutorService getPushTaskTimer() {
        return (ScheduledExecutorService) pools.computeIfAbsent("push-task-timer"
                , s -> executorFactory.get(ExecutorFactory.PUSH_TASK));
    }

    public ScheduledExecutorService getAckTimer() {
        return (ScheduledExecutorService) pools.computeIfAbsent("ack-timer"
                , s -> executorFactory.get(ExecutorFactory.ACK_TIMER));
    }

    public void register(String name, Executor executor) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(executor);
        pools.put(name, executor);
    }

    public Map<String, Executor> getActivePools() {
        return pools;
    }

    public static Map<String, Object> getPoolInfo(ThreadPoolExecutor executor) {
        Map<String, Object> info = new HashMap<>(5);
        info.put("corePoolSize", executor.getCorePoolSize());
        info.put("maxPoolSize", executor.getMaximumPoolSize());
        info.put("activeCount(workingThread)", executor.getActiveCount());
        info.put("poolSize(workThread)", executor.getPoolSize());
        info.put("queueSize(blockedTask)", executor.getQueue().size());
        return info;
    }

    public static Map<String, Object> getPoolInfo(EventLoopGroup executors) {
        Map<String, Object> info = new HashMap<>(3);
        int poolSize = 0, queueSize = 0, activeCount = 0;
        for (EventExecutor e : executors) {
            poolSize++;
            if (e instanceof SingleThreadEventLoop) {
                SingleThreadEventLoop executor = (SingleThreadEventLoop) e;
                queueSize += executor.pendingTasks();
                ThreadProperties tp = executor.threadProperties();
                if (tp.state() == Thread.State.RUNNABLE) {
                    activeCount++;
                }
            }
        }
        info.put("poolSize(workThread)", poolSize);
        info.put("activeCount(workingThread)", activeCount);
        info.put("queueSize(blockedTask)", queueSize);
        return info;
    }
}
