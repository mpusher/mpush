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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public final class ThreadPoolManager {
    public static final ThreadPoolManager I = new ThreadPoolManager();

    private final ExecutorFactory executorFactory = ExecutorFactory.create();
    private final NamedThreadFactory threadFactory = new NamedThreadFactory();

    private Executor eventBusExecutor;
    private Executor redisExecutor;
    private Executor pushCallbackExecutor;
    private ScheduledExecutorService pushCenterTimer;

    public final Thread newThread(String name, Runnable target) {
        return threadFactory.newThread(name, target);
    }

    public Executor getRedisExecutor() {
        if (redisExecutor == null) {
            synchronized (this) {
                redisExecutor = executorFactory.get(ExecutorFactory.MQ);
            }
        }
        return redisExecutor;
    }

    public Executor getEventBusExecutor() {
        if (eventBusExecutor == null) {
            synchronized (this) {
                eventBusExecutor = executorFactory.get(ExecutorFactory.EVENT_BUS);
            }
        }
        return eventBusExecutor;
    }

    public Executor getPushCallbackExecutor() {
        if (pushCallbackExecutor == null) {
            synchronized (this) {
                pushCallbackExecutor = executorFactory.get(ExecutorFactory.PUSH_CALLBACK);
            }
        }
        return pushCallbackExecutor;
    }

    public ScheduledExecutorService getPushCenterTimer() {
        if (pushCenterTimer == null) {
            synchronized (this) {
                pushCenterTimer = (ScheduledExecutorService) executorFactory.get(ExecutorFactory.PUSH_TIMER);
            }
        }
        return pushCenterTimer;
    }

    public Map<String, Executor> getActivePools() {
        Map<String, Executor> map = new HashMap<>();
        if (eventBusExecutor != null) map.put("eventBusExecutor", eventBusExecutor);
        if (redisExecutor != null) map.put("redisExecutor", redisExecutor);
        if (pushCallbackExecutor != null) map.put("pushCallbackExecutor", pushCallbackExecutor);
        if (pushCenterTimer != null) map.put("pushCenterTimer", pushCenterTimer);
        return map;
    }

    public static Map<String, Object> getPoolInfo(ThreadPoolExecutor executor) {
        Map<String, Object> info = new HashMap<>();
        info.put("corePoolSize", executor.getCorePoolSize());
        info.put("maxPoolSize", executor.getMaximumPoolSize());
        info.put("activeCount(workingThread)", executor.getActiveCount());
        info.put("poolSize(workThread)", executor.getPoolSize());
        info.put("queueSize(blockedTask)", executor.getQueue().size());
        return info;
    }
}
