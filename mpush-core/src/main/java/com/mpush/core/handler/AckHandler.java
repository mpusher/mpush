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

package com.mpush.core.handler;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.AckMessage;
import com.mpush.core.MPushServer;
import com.mpush.core.ack.AckTask;
import com.mpush.core.ack.AckTaskQueue;
import com.mpush.tools.log.Logs;

/**
 * Created by ohun on 16/9/5.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AckHandler extends BaseMessageHandler<AckMessage> {

    private final AckTaskQueue ackTaskQueue;

    public AckHandler(MPushServer mPushServer) {
        this.ackTaskQueue = mPushServer.getPushCenter().getAckTaskQueue();
    }


    @Override
    public AckMessage decode(Packet packet, Connection connection) {
        return new AckMessage(packet, connection);
    }

    @Override
    public void handle(AckMessage message) {
        AckTask task = ackTaskQueue.getAndRemove(message.getSessionId());
        if (task == null) {//ack 超时了
            Logs.PUSH.info("receive client ack, but task timeout message={}", message);
            return;
        }

        task.onResponse();//成功收到客户的ACK响应
    }
}
