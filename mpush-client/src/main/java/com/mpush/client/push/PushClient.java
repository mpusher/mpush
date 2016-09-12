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
import com.mpush.api.push.*;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.client.gateway.GatewayClientFactory;
import com.mpush.common.router.ConnectionRouterManager;
import com.mpush.common.router.RemoteRouter;
import com.mpush.tools.Jsons;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKServerNodeWatcher;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.FutureTask;

import static com.mpush.zk.ZKPath.GATEWAY_SERVER;

/*package*/ final class PushClient extends BaseService implements PushSender {
    private static final int DEFAULT_TIMEOUT = 3000;
    private final GatewayClientFactory factory = GatewayClientFactory.I;
    private final ConnectionRouterManager routerManager = ConnectionRouterManager.I;

    private FutureTask<Boolean> send0(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return PushRequest.build(this, ctx).broadcast();
        } else {
            Set<RemoteRouter> routers = routerManager.lookupAll(ctx.getUserId());
            if (routers == null || routers.isEmpty()) {
                return PushRequest.build(this, ctx).offline();
            }
            FutureTask<Boolean> task = null;
            for (RemoteRouter router : routers) {
                task = PushRequest.build(this, ctx).send(router);
            }
            return task;
        }
    }

    @Override
    public FutureTask<Boolean> send(PushContext ctx) {
        if (ctx.getUserId() != null) {
            return send0(ctx);
        } else if (ctx.getUserIds() != null) {
            FutureTask<Boolean> task = null;
            for (String userId : ctx.getUserIds()) {
                task = send0(ctx.setUserId(userId));
            }
            return task;
        } else if (ctx.isBroadcast()) {
            return send0(ctx.setUserId(null));
        } else {
            throw new PushException("param error.");
        }
    }

    Connection getGatewayConnection(String host) {
        return factory.getConnection(host);
    }

    Collection<Connection> getAllConnections() {
        return factory.getAllConnections();
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
