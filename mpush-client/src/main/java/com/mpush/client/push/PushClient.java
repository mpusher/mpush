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

import com.mpush.api.MPushContext;
import com.mpush.api.push.PushContext;
import com.mpush.api.push.PushException;
import com.mpush.api.push.PushResult;
import com.mpush.api.push.PushSender;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.api.spi.common.CacheManagerFactory;
import com.mpush.api.spi.common.ServiceDiscoveryFactory;
import com.mpush.client.MPushClient;
import com.mpush.client.gateway.connection.GatewayConnectionFactory;
import com.mpush.common.router.CachedRemoteRouterManager;
import com.mpush.common.router.RemoteRouter;

import java.util.Set;
import java.util.concurrent.FutureTask;

public final class PushClient extends BaseService implements PushSender {

    private MPushClient mPushClient;

    private PushRequestBus pushRequestBus;

    private CachedRemoteRouterManager cachedRemoteRouterManager;

    private GatewayConnectionFactory gatewayConnectionFactory;

    private FutureTask<PushResult> send0(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return PushRequest.build(mPushClient, ctx).broadcast();
        } else {
            Set<RemoteRouter> remoteRouters = cachedRemoteRouterManager.lookupAll(ctx.getUserId());
            if (remoteRouters == null || remoteRouters.isEmpty()) {
                return PushRequest.build(mPushClient, ctx).onOffline();
            }
            FutureTask<PushResult> task = null;
            for (RemoteRouter remoteRouter : remoteRouters) {
                task = PushRequest.build(mPushClient, ctx).send(remoteRouter);
            }
            return task;
        }
    }

    @Override
    public FutureTask<PushResult> send(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return send0(ctx.setUserId(null));
        } else if (ctx.getUserId() != null) {
            return send0(ctx);
        } else if (ctx.getUserIds() != null) {
            FutureTask<PushResult> task = null;
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
        if (mPushClient == null) {
            mPushClient = new MPushClient();
        }

        pushRequestBus = mPushClient.getPushRequestBus();
        cachedRemoteRouterManager = mPushClient.getCachedRemoteRouterManager();
        gatewayConnectionFactory = mPushClient.getGatewayConnectionFactory();

        ServiceDiscoveryFactory.create().syncStart();
        CacheManagerFactory.create().init();
        pushRequestBus.syncStart();
        gatewayConnectionFactory.start(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        ServiceDiscoveryFactory.create().syncStop();
        CacheManagerFactory.create().destroy();
        pushRequestBus.syncStop();
        gatewayConnectionFactory.stop(listener);
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    @Override
    public void setMPushContext(MPushContext context) {
        this.mPushClient = ((MPushClient) context);
    }
}
