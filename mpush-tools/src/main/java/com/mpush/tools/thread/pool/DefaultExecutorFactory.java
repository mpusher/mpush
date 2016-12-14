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

import com.mpush.api.push.PushException;
import com.mpush.api.spi.Spi;
import com.mpush.api.spi.common.ExecutorFactory;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.NamedPoolThreadFactory;

import java.util.concurrent.*;

import static com.mpush.tools.config.CC.mp.thread.pool.push_center.min;
import static com.mpush.tools.thread.ThreadNames.*;

/**
 * 此线程池可伸缩，线程空闲一定时间后回收，新请求重新创建线程
 */
@Spi(order = 1)
public final class DefaultExecutorFactory implements ExecutorFactory {

    private Executor get(ThreadPoolConfig config) {
        String name = config.getName();
        int corePoolSize = config.getCorePoolSize();
        int maxPoolSize = config.getMaxPoolSize();
        int keepAliveSeconds = config.getKeepAliveSeconds();
        BlockingQueue<Runnable> queue = config.getQueue();

        return new DefaultExecutor(corePoolSize
                , maxPoolSize
                , keepAliveSeconds
                , TimeUnit.SECONDS
                , queue
                , new NamedPoolThreadFactory(name)
                , new DumpThreadRejectedHandler(config));
    }

    @Override
    public Executor get(String name) {
        final ThreadPoolConfig config;
        switch (name) {
            case EVENT_BUS:
                config = ThreadPoolConfig
                        .build(T_EVENT_BUS)
                        .setCorePoolSize(CC.mp.thread.pool.event_bus.min)
                        .setMaxPoolSize(CC.mp.thread.pool.event_bus.max)
                        .setKeepAliveSeconds(TimeUnit.SECONDS.toSeconds(10))
                        .setQueueCapacity(CC.mp.thread.pool.event_bus.queue_size);
                break;
            case MQ:
                config = ThreadPoolConfig
                        .buildFixed(T_MQ,
                                CC.mp.thread.pool.mq.min,
                                CC.mp.thread.pool.mq.queue_size
                        );
                break;
            case PUSH_CALLBACK:
                config = ThreadPoolConfig
                        .build(T_PUSH_CALLBACK)
                        .setCorePoolSize(CC.mp.thread.pool.push_callback.min)
                        .setMaxPoolSize(CC.mp.thread.pool.push_callback.max)
                        .setKeepAliveSeconds(TimeUnit.SECONDS.toSeconds(10))
                        .setQueueCapacity(CC.mp.thread.pool.push_callback.queue_size)
                        .setRejectedPolicy(ThreadPoolConfig.REJECTED_POLICY_CALLER_RUNS);
                break;
            case PUSH_TIMER:
                return new ScheduledThreadPoolExecutor(min, new NamedPoolThreadFactory(T_PUSH_CENTER_TIMER),
                        (r, e) -> {
                            throw new PushException("one push request was rejected. task=" + r);
                        }
                );
            default:
                throw new IllegalArgumentException("no executor for " + name);
        }

        return get(config);
    }
}
