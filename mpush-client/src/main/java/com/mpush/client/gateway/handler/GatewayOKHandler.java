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

package com.mpush.client.gateway.handler;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.client.MPushClient;
import com.mpush.client.push.PushRequest;
import com.mpush.client.push.PushRequestBus;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.OkMessage;
import com.mpush.common.push.GatewayPushResult;
import com.mpush.tools.log.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 16/10/21.
 *
 * @author ohun@live.cn (夜色)
 */
public final class GatewayOKHandler extends BaseMessageHandler<OkMessage> {

    private PushRequestBus pushRequestBus;

    public GatewayOKHandler(MPushClient mPushClient) {
        this.pushRequestBus = mPushClient.getPushRequestBus();
    }

    @Override
    public OkMessage decode(Packet packet, Connection connection) {
        return new OkMessage(packet, connection);
    }

    @Override
    public void handle(OkMessage message) {
        if (message.cmd == Command.GATEWAY_PUSH.cmd) {
            PushRequest request = pushRequestBus.getAndRemove(message.getSessionId());
            if (request == null) {
                Logs.PUSH.warn("receive a gateway response, but request has timeout. message={}", message);
                return;
            }
            request.onSuccess(GatewayPushResult.fromJson(message.data));//推送成功
        }
    }
}
