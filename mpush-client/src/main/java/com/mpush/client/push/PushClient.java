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

import com.mpush.api.Constants;
import com.mpush.api.connection.Connection;
import com.mpush.api.push.PushSender;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.client.gateway.GatewayClientFactory;
import com.mpush.common.router.ConnectionRouterManager;
import com.mpush.common.router.RemoteRouter;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKServerNodeWatcher;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.FutureTask;

import static com.mpush.zk.ZKPath.GATEWAY_SERVER;

/*package*/ class PushClient extends BaseService implements PushSender {
    private static final int DEFAULT_TIMEOUT = 3000;
    private final GatewayClientFactory factory = GatewayClientFactory.I;
    private final ConnectionRouterManager routerManager = ConnectionRouterManager.I;

    public void send(String content, Collection<String> userIds, Callback callback) {
        send(content.getBytes(Constants.UTF_8), userIds, callback);
    }

    @Override
    public FutureTask<Boolean> send(String content, String userId, Callback callback) {
        return send(content.getBytes(Constants.UTF_8), userId, callback);
    }

    @Override
    public void send(byte[] content, Collection<String> userIds, Callback callback) {
        for (String userId : userIds) {
            send(content, userId, callback);
        }
    }

    @Override
    public FutureTask<Boolean> send(byte[] content, String userId, Callback callback) {
        Set<RemoteRouter> routers = routerManager.lookupAll(userId);
        if (routers == null || routers.isEmpty()) {
            return PushRequest
                    .build(this)
                    .setCallback(callback)
                    .setUserId(userId)
                    .setContent(content)
                    .setTimeout(DEFAULT_TIMEOUT)
                    .offline();

        }
        FutureTask<Boolean> task = null;
        for (RemoteRouter router : routers) {
            task = PushRequest
                    .build(this)
                    .setCallback(callback)
                    .setUserId(userId)
                    .setContent(content)
                    .setTimeout(DEFAULT_TIMEOUT)
                    .send(router);
        }
        return task;
    }

    public Connection getGatewayConnection(String host) {
        return factory.getConnection(host);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        ZKClient.I.start(listener);
        RedisManager.I.init();
        ZKServerNodeWatcher.build(GATEWAY_SERVER, factory).beginWatch();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        factory.clear();
        ZKClient.I.stop(listener);
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }
}
