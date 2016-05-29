/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.client.push;

import com.google.common.base.Strings;
import com.mpush.api.connection.Connection;
import com.mpush.api.push.PushSender;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.client.gateway.GatewayClientFactory;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.zk.listener.ZKServerNodeWatcher;
import com.sun.istack.internal.NotNull;

import java.util.Collection;

import static com.mpush.zk.ZKPath.GATEWAY_SERVER;

/*package*/ class PushClient extends BaseService implements PushSender {
    private static final int DEFAULT_TIMEOUT = 3000;
    private final GatewayClientFactory factory = GatewayClientFactory.I;

    public void send(String content, Collection<String> userIds, Callback callback) {
        if (Strings.isNullOrEmpty(content)) return;
        for (String userId : userIds) {
            PushRequest
                    .build(this)
                    .setCallback(callback)
                    .setUserId(userId)
                    .setContent(content)
                    .setTimeout(DEFAULT_TIMEOUT)
                    .send();
        }
    }

    @Override
    public void send(String content, String userId, Callback callback) {
        PushRequest
                .build(this)
                .setCallback(callback)
                .setUserId(userId)
                .setContent(content)
                .setTimeout(DEFAULT_TIMEOUT)
                .send();
    }

    public Connection getGatewayConnection(String host) {
        return factory.getConnection(host);
    }

    @Override
    protected void doStart(@NotNull Listener listener) throws Throwable {
        ZKClient.I.start(listener);
        RedisManager.I.init();
        ZKServerNodeWatcher.build(GATEWAY_SERVER, factory).beginWatch();
    }

    @Override
    protected void doStop(@NotNull Listener listener) throws Throwable {
        factory.clear();
        ZKClient.I.stop(listener);
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }
}
