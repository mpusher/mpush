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

import com.mpush.api.spi.push.PushListener;
import com.mpush.api.spi.Spi;
import com.mpush.api.spi.push.PushListenerFactory;
import com.mpush.common.ErrorCode;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.tools.log.Logs;

import static com.mpush.common.ErrorCode.*;

/**
 * Created by ohun on 2016/12/24.
 *
 * @author ohun@live.cn (夜色)
 */
@Spi(order = 1)
public final class GatewayPushListener implements PushListener<GatewayPushMessage>, PushListenerFactory<GatewayPushMessage> {

    @Override
    public void onSuccess(GatewayPushMessage message) {
        if (message.getConnection().isConnected()) {
            OkMessage
                    .from(message)
                    .setData(message.userId + ',' + message.clientType)
                    .sendRaw();
        } else {
            Logs.PUSH.warn("push message to client success, but gateway connection is closed, message={}", message);
        }
    }

    @Override
    public void onAckSuccess(GatewayPushMessage message) {
        if (message.getConnection().isConnected()) {
            OkMessage
                    .from(message)
                    .setData(message.userId + ',' + message.clientType)
                    .sendRaw();
        } else {
            Logs.PUSH.warn("client ack success, but gateway connection is closed, message={}", message);
        }
    }

    @Override
    public void onBroadcastComplete(GatewayPushMessage message) {
        if (message.getConnection().isConnected()) {
            OkMessage
                    .from(message)
                    .sendRaw();
        } else {
            Logs.PUSH.warn("broadcast to client finish, but gateway connection is closed, message={}", message);
        }
    }

    @Override
    public void onFailure(GatewayPushMessage message) {
        if (message.getConnection().isConnected()) {
            ErrorMessage
                    .from(message)
                    .setErrorCode(PUSH_CLIENT_FAILURE)
                    .setData(message.userId + ',' + message.clientType)
                    .sendRaw();
        } else {
            Logs.PUSH.warn("push message to client failure, but gateway connection is closed, message={}", message);
        }
    }

    @Override
    public void onOffline(GatewayPushMessage message) {
        if (message.getConnection().isConnected()) {
            ErrorMessage
                    .from(message)
                    .setErrorCode(OFFLINE)
                    .setData(message.userId + ',' + message.clientType)
                    .sendRaw();
        } else {
            Logs.PUSH.warn("push message to client offline, but gateway connection is closed, message={}", message);
        }
    }

    @Override
    public void onRedirect(GatewayPushMessage message) {
        if (message.getConnection().isConnected()) {
            ErrorMessage
                    .from(message)
                    .setErrorCode(ROUTER_CHANGE)
                    .setData(message.userId + ',' + message.clientType)
                    .sendRaw();
        } else {
            Logs.PUSH.warn("push message to client redirect, but gateway connection is closed, message={}", message);
        }
    }


    @Override
    public void onAckTimeout(GatewayPushMessage message) {
        if (message.getConnection().isConnected()) {
            ErrorMessage
                    .from(message)
                    .setData(message.userId + ',' + message.clientType)
                    .setErrorCode(ErrorCode.ACK_TIMEOUT)
                    .sendRaw();
        } else {
            Logs.PUSH.warn("push message to client ackTimeout, but gateway connection is closed, message={}", message);
        }
    }

    @Override
    public PushListener<GatewayPushMessage> get() {
        return this;
    }
}
