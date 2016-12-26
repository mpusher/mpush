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

/**
 * Created by ohun on 2016/12/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class MQPushMessageReceiver {

    public MessagePusher pusher = MessagePusherFactory.create();

    public void init() {

    }

    //receiver message form mq
    public void onMessage(MQPushMessage message) {
        pusher.push(message);
    }

    //fetch message form mq
    public MQPushMessage fetch() {
        return new MQPushMessage();
    }

    public void dispatch() {
        while (true) {
            MQPushMessage message = fetch();
            if (message == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                continue;
            }
            onMessage(message);
        }
    }
}
