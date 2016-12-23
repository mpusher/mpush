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
import com.mpush.core.ack.AckTaskQueue;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class PushCenter extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final PushCenter I = new PushCenter();

    private final AtomicLong taskNum = new AtomicLong();

    private PushTaskExecutor executor;

    private PushCenter() {
    }

    public void addTask(PushTask task) {
        executor.addTask(task);
        logger.debug("add new task to push center, count={}, task={}", taskNum.incrementAndGet(), task);
    }

    public void delayTask(long delay, PushTask task) {
        executor.delayTask(delay, task);
        logger.debug("delay task to push center, count={}, task={}", taskNum.incrementAndGet(), task);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        if (CC.mp.net.udpGateway()) {
            executor = new CustomJDKExecutor(ThreadPoolManager.I.getPushTaskTimer());
        } else {//实际情况使用EventLoo并没有更快，还有待测试
            executor = new CustomJDKExecutor(ThreadPoolManager.I.getPushTaskTimer());
            //executor = new NettyEventLoopExecutor();
        }

        AckTaskQueue.I.start();
        logger.info("push center start success");
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        executor.shutdown();
        AckTaskQueue.I.stop();
        logger.info("push center stop success");
        listener.onSuccess();
    }

    /**
     * TCP 模式直接使用GatewayServer work 线程池
     */
    private static class NettyEventLoopExecutor implements PushTaskExecutor {

        @Override
        public void shutdown() {
        }

        @Override
        public void addTask(PushTask task) {
            task.getExecutor().execute(task);
        }

        @Override
        public void delayTask(long delay, PushTask task) {
            task.getExecutor().schedule(task, delay, TimeUnit.NANOSECONDS);
        }
    }


    /**
     * UDP 模式使用自定义线程池
     */
    private static class CustomJDKExecutor implements PushTaskExecutor {
        private final ScheduledExecutorService executorService;

        private CustomJDKExecutor(ScheduledExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public void shutdown() {
            executorService.shutdown();
        }

        @Override
        public void addTask(PushTask task) {
            executorService.execute(task);
        }

        @Override
        public void delayTask(long delay, PushTask task) {
            executorService.schedule(task, delay, TimeUnit.NANOSECONDS);
        }
    }

    private interface PushTaskExecutor {

        void shutdown();

        void addTask(PushTask task);

        void delayTask(long delay, PushTask task);
    }
}
