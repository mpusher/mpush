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
import com.mpush.common.message.OkMessage;
import com.mpush.core.ack.AckContext;
import com.mpush.core.ack.AckMessageQueue;
import com.mpush.tools.log.Logs;

import static com.mpush.api.protocol.Command.OK;

/**
 * Created by ohun on 16/9/5.
 *
 * @author ohun@live.cn (夜色)
 */
public class AckHandler extends BaseMessageHandler<AckMessage> {

    @Override
    public AckMessage decode(Packet packet, Connection connection) {
        return new AckMessage(packet, connection);
    }

    @Override
    public void handle(AckMessage message) {
        AckContext context = AckMessageQueue.I.getAndRemove(message.getSessionId());
        if (context == null) {
            Logs.PUSH.info("receive client ack, but timeout message={}", message);
            return;
        }

        context.success();
    }
}
