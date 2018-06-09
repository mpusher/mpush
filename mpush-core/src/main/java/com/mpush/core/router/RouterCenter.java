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

package com.mpush.core.router;

import com.mpush.api.connection.Connection;
import com.mpush.api.event.RouterChangeEvent;
import com.mpush.api.router.ClientLocation;
import com.mpush.api.router.Router;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.common.router.RemoteRouter;
import com.mpush.common.router.RemoteRouterManager;
import com.mpush.core.MPushServer;
import com.mpush.tools.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class RouterCenter extends BaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterCenter.class);

    private LocalRouterManager localRouterManager;
    private RemoteRouterManager remoteRouterManager;
    private UserEventConsumer userEventConsumer;
    private RouterChangeListener routerChangeListener;
    private MPushServer mPushServer;

    public RouterCenter(MPushServer mPushServer) {
        this.mPushServer = mPushServer;
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        localRouterManager = new LocalRouterManager();
        remoteRouterManager = new RemoteRouterManager();
        routerChangeListener = new RouterChangeListener(mPushServer);
        userEventConsumer = new UserEventConsumer(remoteRouterManager);
        userEventConsumer.getUserManager().clearOnlineUserList();
        super.doStart(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        userEventConsumer.getUserManager().clearOnlineUserList();
        super.doStop(listener);
    }

    /**
     * 注册用户和链接
     *
     * @param userId
     * @param connection
     * @return
     */
    public boolean register(String userId, Connection connection) {
        ClientLocation location = ClientLocation
                .from(connection)
                .setHost(mPushServer.getGatewayServerNode().getHost())
                .setPort(mPushServer.getGatewayServerNode().getPort());

        LocalRouter localRouter = new LocalRouter(connection);
        RemoteRouter remoteRouter = new RemoteRouter(location);

        LocalRouter oldLocalRouter = null;
        RemoteRouter oldRemoteRouter = null;
        try {
            oldLocalRouter = localRouterManager.register(userId, localRouter);
            oldRemoteRouter = remoteRouterManager.register(userId, remoteRouter);
        } catch (Exception e) {
            LOGGER.error("register router ex, userId={}, connection={}", userId, connection, e);
        }

        if (oldLocalRouter != null) {
            EventBus.post(new RouterChangeEvent(userId, oldLocalRouter));
            LOGGER.info("register router success, find old local router={}, userId={}", oldLocalRouter, userId);
        }

        if (oldRemoteRouter != null && oldRemoteRouter.isOnline()) {
            EventBus.post(new RouterChangeEvent(userId, oldRemoteRouter));
            LOGGER.info("register router success, find old remote router={}, userId={}", oldRemoteRouter, userId);
        }
        return true;
    }

    public boolean unRegister(String userId, int clientType) {
        localRouterManager.unRegister(userId, clientType);
        remoteRouterManager.unRegister(userId, clientType);
        return true;
    }

    public Router<?> lookup(String userId, int clientType) {
        LocalRouter local = localRouterManager.lookup(userId, clientType);
        if (local != null) return local;
        RemoteRouter remote = remoteRouterManager.lookup(userId, clientType);
        return remote;
    }

    public LocalRouterManager getLocalRouterManager() {
        return localRouterManager;
    }

    public RemoteRouterManager getRemoteRouterManager() {
        return remoteRouterManager;
    }

    public RouterChangeListener getRouterChangeListener() {
        return routerChangeListener;
    }

    public UserEventConsumer getUserEventConsumer() {
        return userEventConsumer;
    }
}
