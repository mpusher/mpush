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

import com.mpush.api.MPushContext;
import com.mpush.api.spi.Spi;
import com.mpush.api.spi.push.PushListener;
import com.mpush.api.spi.push.PushListenerFactory;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.core.MPushServer;
import com.mpush.tools.Jsons;
import com.mpush.tools.log.Logs;

import java.util.concurrent.ScheduledExecutorService;

import static com.mpush.common.ErrorCode.*;
import static com.mpush.common.push.GatewayPushResult.toJson;

/**
 * Created by ohun on 2016/12/24.
 *
 * @author ohun@live.cn (夜色)
 */
@Spi(order = 1)
public final class GatewayPushListener implements PushListener<GatewayPushMessage>, PushListenerFactory<GatewayPushMessage> {

    private PushCenter pushCenter;

    @Override
    public void init(MPushContext context) {
        pushCenter = ((MPushServer) context).getPushCenter();
    }

    @Override
    public void onSuccess(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage
                            .from(message)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to client success, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onAckSuccess(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage
                            .from(message)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });

        } else {
            Logs.PUSH.warn("client ack success, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onBroadcastComplete(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage
                            .from(message)
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("broadcast to client finish, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onFailure(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage
                            .from(message)
                            .setErrorCode(PUSH_CLIENT_FAILURE)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to client failure, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onOffline(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage
                            .from(message)
                            .setErrorCode(OFFLINE)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to client offline, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onRedirect(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage
                            .from(message)
                            .setErrorCode(ROUTER_CHANGE)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to client redirect, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }


    @Override
    public void onTimeout(GatewayPushMessage message, Object[] timePoints) {
        Logs.PUSH.warn("push message to client timeout, timePoints={}, message={}"
                , Jsons.toJson(timePoints), message);
    }

    @Override
    public PushListener<GatewayPushMessage> get() {
        return this;
    }
}
