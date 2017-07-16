/*
 * (C) Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.common;

import com.mpush.api.spi.common.ExecutorFactory;
import com.mpush.tools.config.CC;
import com.mpush.tools.log.Logs;
import com.mpush.tools.thread.NamedPoolThreadFactory;
import com.mpush.tools.thread.pool.DefaultExecutor;
import com.mpush.tools.thread.pool.DumpThreadRejectedHandler;
import com.mpush.tools.thread.pool.ThreadPoolConfig;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.mpush.tools.config.CC.mp.thread.pool.ack_timer;
import static com.mpush.tools.config.CC.mp.thread.pool.push_client;
import static com.mpush.tools.thread.ThreadNames.T_ARK_REQ_TIMER;
import static com.mpush.tools.thread.ThreadNames.T_EVENT_BUS;
import static com.mpush.tools.thread.ThreadNames.T_PUSH_CLIENT_TIMER;

/**
 * Created by ohun on 2017/7/15.
 *
 * @author ohun@live.cn (夜色)
 */
public class CommonExecutorFactory implements ExecutorFactory {
    protected Executor get(ThreadPoolConfig config) {
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
                        .setQueueCapacity(CC.mp.thread.pool.event_bus.queue_size)
                        .setRejectedPolicy(ThreadPoolConfig.REJECTED_POLICY_CALLER_RUNS);
                break;
            case PUSH_CLIENT: {
                ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(push_client
                        , new NamedPoolThreadFactory(T_PUSH_CLIENT_TIMER), (r, e) -> r.run() // run caller thread
                );
                executor.setRemoveOnCancelPolicy(true);
                return executor;
            }
            case ACK_TIMER: {
                ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(ack_timer,
                        new NamedPoolThreadFactory(T_ARK_REQ_TIMER),
                        (r, e) -> Logs.PUSH.error("one ack context was rejected, context=" + r)
                );
                executor.setRemoveOnCancelPolicy(true);
                return executor;
            }
            default:
                throw new IllegalArgumentException("no executor for " + name);
        }

        return get(config);
    }
}
