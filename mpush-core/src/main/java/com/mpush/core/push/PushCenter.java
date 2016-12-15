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

package com.mpush.core.push;

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.core.ack.AckMessageQueue;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class PushCenter extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final PushCenter I = new PushCenter();

    private ScheduledExecutorService executor;

    private PushCenter() {
    }

    public void addTask(PushTask task) {
        executor.execute(task);
        logger.debug("add new task to push center, task={}", task);
    }

    public void delayTask(int delay, PushTask task) {
        executor.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        executor = ThreadPoolManager.I.getPushTaskTimer();
        AckMessageQueue.I.start();
        logger.info("push center start success");
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        executor.shutdown();
        AckMessageQueue.I.stop();
        logger.info("push center stop success");
        listener.onSuccess();
    }
}
