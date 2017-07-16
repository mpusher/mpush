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

import com.mpush.api.MPushContext;
import com.mpush.api.spi.Spi;
import com.mpush.api.spi.push.PushListener;
import com.mpush.api.spi.push.PushListenerFactory;
import com.mpush.core.MPushServer;

/**
 * Created by ohun on 2016/12/24.
 *
 * @author ohun@live.cn (夜色)
 */
@Spi(order = 2)
public final class MQPushListener implements PushListener<MQPushMessage>, PushListenerFactory<MQPushMessage> {
    private final MQClient mqClient = new MQClient();

    @Override
    public void init(MPushContext context) {
        mqClient.init();
        MQMessageReceiver.subscribe(mqClient, ((MPushServer) context).getPushCenter());
    }


    @Override
    public void onSuccess(MQPushMessage message, Object[] timePoints) {
        //publish messageId to mq:[success/queue]
        mqClient.publish("/mpush/push/success", message);
    }

    @Override
    public void onAckSuccess(MQPushMessage message, Object[] timePoints) {
        //publish messageId to mq:[success/queue]
        mqClient.publish("/mpush/push/success", message);
    }

    @Override
    public void onBroadcastComplete(MQPushMessage message, Object[] timePoints) {
        //publish messageId to mq:[broadcast/finish/queue]
        mqClient.publish("/mpush/push/broadcast_finish", message);
    }

    @Override
    public void onFailure(MQPushMessage message, Object[] timePoints) {
        //publish messageId to mq:[failure/queue], client can retry
        mqClient.publish("/mpush/push/failure", message);
    }

    @Override
    public void onOffline(MQPushMessage message, Object[] timePoints) {
        //publish messageId to mq:[offline/queue], client persist offline message to db
        mqClient.publish("/mpush/push/offline", message);
    }

    @Override
    public void onRedirect(MQPushMessage message, Object[] timePoints) {
        //publish messageId to mq:[route/change/queue], client should be try again
        mqClient.publish("/mpush/push/route_change", message);
    }

    @Override
    public void onTimeout(MQPushMessage message, Object[] timePoints) {
        //publish messageId to mq:[ack/timeout/queue], client can retry
        mqClient.publish("/mpush/push/ack_timeout", message);
    }

    @Override
    public PushListener<MQPushMessage> get() {
        return this;
    }
}
