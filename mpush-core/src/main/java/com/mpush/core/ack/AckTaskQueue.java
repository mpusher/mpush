/*
 * (C) Copyright 2015-2016 the original author or authors.
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

package com.mpush.core.ack;

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.core.MPushServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 16/9/5.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AckTaskQueue extends BaseService {
    private static final int DEFAULT_TIMEOUT = 3000;

    private final Logger logger = LoggerFactory.getLogger(AckTaskQueue.class);

    private final ConcurrentMap<Integer, AckTask> queue = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutor;
    private MPushServer mPushServer;

    public AckTaskQueue(MPushServer mPushServer) {
        this.mPushServer = mPushServer;
    }

    public void add(AckTask task, int timeout) {
        queue.put(task.getAckMessageId(), task);
        task.setAckTaskQueue(this);
        task.setFuture(scheduledExecutor.schedule(task,//使用 task.getExecutor() 并没更快
                timeout > 0 ? timeout : DEFAULT_TIMEOUT,
                TimeUnit.MILLISECONDS
        ));

        logger.debug("one ack task add to queue, task={}, timeout={}", task, timeout);
    }

    public AckTask getAndRemove(int sessionId) {
        return queue.remove(sessionId);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        scheduledExecutor = mPushServer.getMonitor().getThreadPoolManager().getAckTimer();
        super.doStart(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        super.doStop(listener);
    }
}
