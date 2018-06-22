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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.ConnectionCloseEvent;
import com.mpush.api.event.UserOfflineEvent;
import com.mpush.api.router.RouterManager;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.event.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class LocalRouterManager extends EventConsumer implements RouterManager<LocalRouter> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalRouterManager.class);
    private static final Map<Integer, LocalRouter> EMPTY = new HashMap<>(0);

    /**
     * 本地路由表
     */
    private final Map<String, Map<Integer, LocalRouter>> routers = new ConcurrentHashMap<>();

    @Override
    public LocalRouter register(String userId, LocalRouter router) {
        LOGGER.info("register local router success userId={}, router={}", userId, router);
        return routers.computeIfAbsent(userId, s -> new HashMap<>(1)).put(router.getClientType(), router);
    }

    @Override
    public boolean unRegister(String userId, int clientType) {
        LocalRouter router = routers.getOrDefault(userId, EMPTY).remove(clientType);
        LOGGER.info("unRegister local router success userId={}, router={}", userId, router);
        return true;
    }

    @Override
    public Set<LocalRouter> lookupAll(String userId) {
        return new HashSet<>(routers.getOrDefault(userId, EMPTY).values());
    }

    @Override
    public LocalRouter lookup(String userId, int clientType) {
        LocalRouter router = routers.getOrDefault(userId, EMPTY).get(clientType);
        LOGGER.info("lookup local router userId={}, router={}", userId, router);
        return router;
    }

    public Map<String, Map<Integer, LocalRouter>> routers() {
        return routers;
    }

    /**
     * 监听链接关闭事件，清理失效的路由
     *
     * @param event
     */
    @Subscribe
    @AllowConcurrentEvents
    void on(ConnectionCloseEvent event) {
        Connection connection = event.connection;
        if (connection == null) return;
        SessionContext context = connection.getSessionContext();

        String userId = context.userId;
        if (userId == null) return;

        int clientType = context.getClientType();
        LocalRouter localRouter = routers.getOrDefault(userId, EMPTY).get(clientType);
        if (localRouter == null) return;

        String connId = connection.getId();
        //2.检测下，是否是同一个链接, 如果客户端重连，老的路由会被新的链接覆盖
        if (connId.equals(localRouter.getRouteValue().getId())) {
            //3. 删除路由
            routers.getOrDefault(userId, EMPTY).remove(clientType);
            //4. 发送用户下线事件, 只有老的路由存在的情况下才发送，因为有可能又用户重连了，而老的链接又是在新连接之后才断开的
            //这个时候就会有问题，会导致用户变成下线状态，实际用户应该是在线的。
            EventBus.post(new UserOfflineEvent(event.connection, userId));
            LOGGER.info("clean disconnected local route, userId={}, route={}", userId, localRouter);
        } else { //如果不相等，则log一下
            LOGGER.info("clean disconnected local route, not clean:userId={}, route={}", userId, localRouter);
        }
    }
}
