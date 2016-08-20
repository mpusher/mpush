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

package com.mpush.common.router;

import com.google.common.eventbus.Subscribe;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.ConnectionCloseEvent;
import com.mpush.api.router.ClientLocation;
import com.mpush.api.router.ClientType;
import com.mpush.api.router.RouterManager;
import com.mpush.cache.redis.RedisKey;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.tools.event.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public class RemoteRouterManager extends EventConsumer implements RouterManager<RemoteRouter> {
    public static final Logger LOGGER = LoggerFactory.getLogger(RemoteRouterManager.class);

    @Override
    public RemoteRouter register(String userId, RemoteRouter router) {
        LOGGER.info("register remote router success userId={}, router={}", userId, router);
        String key = RedisKey.getUserKey(userId);
        String field = Integer.toString(router.getRouteValue().getClientType());
        ClientLocation old = RedisManager.I.hget(key, field, ClientLocation.class);
        if (old != null) {
            RedisManager.I.hdel(key, field);
        }
        RedisManager.I.hset(key, field, router.getRouteValue());
        return old == null ? null : new RemoteRouter(old);
    }

    @Override
    public boolean unRegister(String userId, int clientType) {
        String key = RedisKey.getUserKey(userId);
        String field = Integer.toString(clientType);
        RedisManager.I.hdel(key, field);
        LOGGER.info("unRegister remote router success userId={}, clientType={}", userId, clientType);
        return true;
    }

    @Override
    public Set<RemoteRouter> lookupAll(String userId) {
        String key = RedisKey.getUserKey(userId);
        Map<String, ClientLocation> values = RedisManager.I.hgetAll(key, ClientLocation.class);
        if (values == null || values.isEmpty()) return Collections.emptySet();
        return values.values().stream().map(RemoteRouter::new).collect(Collectors.toSet());
    }

    @Override
    public RemoteRouter lookup(String userId, int clientType) {
        String key = RedisKey.getUserKey(userId);
        String field = Integer.toString(clientType);
        ClientLocation location = RedisManager.I.hget(key, field, ClientLocation.class);
        LOGGER.info("lookup remote router userId={}, router={}", userId, location);
        return location == null ? null : new RemoteRouter(location);
    }

    /**
     * 监听链接关闭事件，清理失效的路由
     *
     * @param event
     */
    @Subscribe
    void on(ConnectionCloseEvent event) {
        Connection connection = event.connection;
        if (connection == null) return;
        SessionContext context = connection.getSessionContext();
        String userId = context.userId;
        if (userId == null) return;
        String key = RedisKey.getUserKey(userId);
        String field = Integer.toString(context.getClientType());
        ClientLocation location = RedisManager.I.hget(key, field, ClientLocation.class);
        if (location == null) return;

        String connId = connection.getId();
        //2.检测下，是否是同一个链接, 如果客户端重连，老的路由会被新的链接覆盖
        if (connId.equals(location.getConnId())) {
            RedisManager.I.hdel(key, field);
            LOGGER.info("clean disconnected remote route, userId={}, route={}", userId, location);
        } else {
            LOGGER.info("clean disconnected remote route, not clean:userId={}, route={}", userId, location);
        }
    }
}
