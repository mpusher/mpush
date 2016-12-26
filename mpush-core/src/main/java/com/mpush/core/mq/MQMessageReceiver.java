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

package com.mpush.core.mq;

import com.mpush.api.spi.push.MessagePusher;
import com.mpush.api.spi.push.MessagePusherFactory;
import com.mpush.tools.Utils;
import com.mpush.tools.thread.pool.ThreadPoolManager;

import java.util.Collection;

/**
 * Created by ohun on 2016/12/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class MQMessageReceiver {

    private final static String TOPIC = "/mpush/push/" + Utils.getLocalIp();

    public final MessagePusher pusher = MessagePusherFactory.create();

    private final MQClient mqClient;

    public static void subscribe(MQClient mqClient) {
        MQMessageReceiver receiver = new MQMessageReceiver(mqClient);
        mqClient.subscribe(TOPIC, receiver);
        receiver.fetchFormMQ();
    }

    public MQMessageReceiver(MQClient mqClient) {
        this.mqClient = mqClient;
    }

    public void onMessage(MQPushMessage message) {
        pusher.push(message);
    }

    public void fetchFormMQ() {
        ThreadPoolManager.I.newThread("mq-push", this::dispatch);
    }

    private void dispatch() {
        try {
            while (true) {
                Collection<MQPushMessage> message = mqClient.take(TOPIC);
                if (message == null || message.isEmpty()) {
                    Thread.sleep(100);
                    continue;
                }
                message.forEach(this::onMessage);
            }
        } catch (InterruptedException e) {
            this.dispatch();
        }
    }
}
