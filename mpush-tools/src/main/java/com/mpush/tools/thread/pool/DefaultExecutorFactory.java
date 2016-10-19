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

import com.mpush.api.spi.Spi;
import com.mpush.api.spi.common.ExecutorFactory;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.NamedPoolThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.mpush.tools.thread.ThreadNames.*;

/**
 * 此线程池可伸缩，线程空闲一定时间后回收，新请求重新创建线程
 */
@Spi(order = 1)
public class DefaultExecutorFactory implements ExecutorFactory {

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
            case SERVER_BOSS:
                config = ThreadPoolConfig
                        .build(T_SERVER_BOSS)
                        .setCorePoolSize(CC.mp.thread.pool.boss.min)
                        .setMaxPoolSize(CC.mp.thread.pool.boss.max)
                        .setKeepAliveSeconds(TimeUnit.MINUTES.toSeconds(5))
                        .setQueueCapacity(CC.mp.thread.pool.boss.queue_size);
                break;
            case SERVER_WORK:
                config = ThreadPoolConfig
                        .build(T_SERVER_WORKER)
                        .setCorePoolSize(CC.mp.thread.pool.work.min)
                        .setMaxPoolSize(CC.mp.thread.pool.work.max)
                        .setKeepAliveSeconds(TimeUnit.MINUTES.toSeconds(5))
                        .setQueueCapacity(CC.mp.thread.pool.work.queue_size);
                break;
            case HTTP_CLIENT_WORK:
                config = ThreadPoolConfig
                        .buildFixed(T_HTTP_CLIENT,
                                CC.mp.thread.pool.http_proxy.min,
                                CC.mp.thread.pool.http_proxy.queue_size
                        )
                        .setRejectedPolicy(ThreadPoolConfig.REJECTED_POLICY_DISCARD);
                break;
            case EVENT_BUS:
                config = ThreadPoolConfig
                        .buildFixed(T_EVENT_BUS,
                                CC.mp.thread.pool.event_bus.min,
                                CC.mp.thread.pool.event_bus.queue_size
                        );
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
            default:
            case BIZ:
                config = ThreadPoolConfig
                        .build(T_BIZ)
                        .setCorePoolSize(CC.mp.thread.pool.biz.min)
                        .setMaxPoolSize(CC.mp.thread.pool.biz.max)
                        .setKeepAliveSeconds(TimeUnit.MINUTES.toSeconds(5))
                        .setQueueCapacity(CC.mp.thread.pool.biz.queue_size);
                break;
        }

        return get(config);
    }
}
