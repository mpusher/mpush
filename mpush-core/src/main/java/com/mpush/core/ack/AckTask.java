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

import com.mpush.api.Message;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ohun on 16/9/5.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AckTask implements Runnable {
    final int ackMessageId;
    private final Message message;

    private AckCallback callback;
    private Future<?> timeoutFuture;

    public AckTask(Message message, int ackMessageId) {
        this.message = message;
        this.ackMessageId = ackMessageId;
    }

    public static AckTask from(Message message, int ackMessageId) {
        return new AckTask(message, ackMessageId);
    }

    public void setFuture(Future<?> future) {
        this.timeoutFuture = future;
    }

    public ScheduledExecutorService getExecutor() {
        return message.getConnection().getChannel().eventLoop();
    }

    public AckTask setCallback(AckCallback callback) {
        this.callback = callback;
        return this;
    }

    private boolean tryDone() {
        return timeoutFuture.cancel(true);
    }

    public void onResponse() {
        if (tryDone()) {
            callback.onSuccess(this);
            callback = null;
        }
    }

    public void onTimeout() {
        AckTask context = AckTaskQueue.I.getAndRemove(ackMessageId);
        if (context != null && tryDone()) {
            callback.onTimeout(this);
            callback = null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                ", ackMessageId=" + ackMessageId +
                '}';
    }

    @Override
    public void run() {
        onTimeout();
    }
}
