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

import com.mpush.common.message.BaseMessage;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ohun on 16/9/5.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AckContext implements Runnable {
    private final AtomicBoolean done = new AtomicBoolean(false);

    public final int gatewayMessageId;
    public final byte cmd;
    private AckCallback callback;
    /*package*/ int pushMessageId;

    public AckContext(int gatewayMessageId, byte cmd) {
        this.gatewayMessageId = gatewayMessageId;
        this.cmd = cmd;
    }

    public static AckContext from(BaseMessage message) {
        return new AckContext(message.getSessionId(), message.getPacket().cmd);
    }

    public boolean tryDone() {
        return done.compareAndSet(false, true);
    }

    public AckContext setCallback(AckCallback callback) {
        this.callback = callback;
        return this;
    }

    public void success() {
        if (tryDone()) {
            callback.onSuccess(this);
        }
    }

    public void timeout() {
        AckContext context = AckMessageQueue.I.getAndRemove(pushMessageId);
        if (context != null && tryDone()) {
            callback.onTimeout(this);
        }
    }

    @Override
    public String toString() {
        return "AckContext{" +
                "gatewayMessageId=" + gatewayMessageId +
                ", pushMessageId=" + pushMessageId +
                '}';
    }

    @Override
    public void run() {
        timeout();
    }
}
