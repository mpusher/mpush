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

import com.mpush.api.push.PushContext;
import com.mpush.api.push.PushException;
import com.mpush.api.push.PushSender;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.client.gateway.connection.GatewayConnectionFactory;
import com.mpush.client.gateway.connection.GatewayTCPConnectionFactory;
import com.mpush.client.gateway.connection.GatewayUDPConnectionFactory;
import com.mpush.common.router.CachedRemoteRouterManager;
import com.mpush.common.router.RemoteRouter;
import com.mpush.tools.config.CC;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKServerNodeWatcher;

import java.util.Set;
import java.util.concurrent.FutureTask;

import static com.mpush.zk.ZKPath.GATEWAY_SERVER;

/*package*/ final class PushClient extends BaseService implements PushSender {
    private final GatewayConnectionFactory factory = GatewayConnectionFactory.create();

    private FutureTask<Boolean> send0(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return PushRequest.build(factory, ctx).broadcast();
        } else {
            Set<RemoteRouter> remoteRouters = CachedRemoteRouterManager.I.lookupAll(ctx.getUserId());
            if (remoteRouters == null || remoteRouters.isEmpty()) {
                return PushRequest.build(factory, ctx).offline();
            }
            FutureTask<Boolean> task = null;
            for (RemoteRouter remoteRouter : remoteRouters) {
                task = PushRequest.build(factory, ctx).send(remoteRouter);
            }
            return task;
        }
    }

    @Override
    public FutureTask<Boolean> send(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return send0(ctx.setUserId(null));
        } else if (ctx.getUserId() != null) {
            return send0(ctx);
        } else if (ctx.getUserIds() != null) {
            FutureTask<Boolean> task = null;
            for (String userId : ctx.getUserIds()) {
                task = send0(ctx.setUserId(userId));
            }
            return task;
        } else {
            throw new PushException("param error.");
        }
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        ZKClient.I.syncStart();
        RedisManager.I.init();
        ZKServerNodeWatcher.build(GATEWAY_SERVER, factory).watch();
        PushRequestBus.I.syncStart();
        factory.init(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        ZKClient.I.syncStop();
        factory.clear();
        RedisManager.I.destroy();
        PushRequestBus.I.stop(listener);
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }
}
