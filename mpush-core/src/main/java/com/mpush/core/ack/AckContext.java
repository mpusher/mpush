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

import java.util.concurrent.Future;

/**
 * Created by ohun on 16/9/5.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AckContext implements Runnable {
    private AckCallback callback;

    private int sessionId;
    private Future<?> future;

    public AckContext() {
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    public AckContext setCallback(AckCallback callback) {
        this.callback = callback;
        return this;
    }

    private boolean tryDone() {
        return future.cancel(true);
    }

    public void success() {
        if (tryDone()) {
            callback.onSuccess(this);
            callback = null;
        }
    }

    public void timeout() {
        AckContext context = AckMessageQueue.I.getAndRemove(sessionId);
        if (context != null && tryDone()) {
            callback.onTimeout(this);
            callback = null;
        }
    }

    @Override
    public String toString() {
        return "AckContext{" +
                ", sessionId=" + sessionId +
                '}';
    }

    @Override
    public void run() {
        timeout();
    }
}
