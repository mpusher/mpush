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

package com.mpush.core;

import com.mpush.api.push.PushException;
import com.mpush.api.spi.Spi;
import com.mpush.common.CommonExecutorFactory;
import com.mpush.tools.config.CC;
import com.mpush.tools.log.Logs;
import com.mpush.tools.thread.NamedPoolThreadFactory;
import com.mpush.tools.thread.pool.ThreadPoolConfig;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.mpush.tools.config.CC.mp.thread.pool.ack_timer;
import static com.mpush.tools.config.CC.mp.thread.pool.push_task;
import static com.mpush.tools.thread.ThreadNames.*;

/**
 * 此线程池可伸缩，线程空闲一定时间后回收，新请求重新创建线程
 */
@Spi(order = 1)
public final class ServerExecutorFactory extends CommonExecutorFactory {

    @Override
    public Executor get(String name) {
        final ThreadPoolConfig config;
        switch (name) {
            case MQ:
                config = ThreadPoolConfig
                        .build(T_MQ)
                        .setCorePoolSize(CC.mp.thread.pool.mq.min)
                        .setMaxPoolSize(CC.mp.thread.pool.mq.max)
                        .setKeepAliveSeconds(TimeUnit.SECONDS.toSeconds(10))
                        .setQueueCapacity(CC.mp.thread.pool.mq.queue_size)
                        .setRejectedPolicy(ThreadPoolConfig.REJECTED_POLICY_CALLER_RUNS);
                break;
            case PUSH_TASK:
                return new ScheduledThreadPoolExecutor(push_task, new NamedPoolThreadFactory(T_PUSH_CENTER_TIMER),
                        (r, e) -> {
                            throw new PushException("one push task was rejected. task=" + r);
                        }
                );
            case ACK_TIMER: {
                ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(ack_timer,
                        new NamedPoolThreadFactory(T_ARK_REQ_TIMER),
                        (r, e) -> Logs.PUSH.error("one ack context was rejected, context=" + r)
                );
                executor.setRemoveOnCancelPolicy(true);
                return executor;
            }
            default:
                return super.get(name);
        }

        return get(config);
    }
}
