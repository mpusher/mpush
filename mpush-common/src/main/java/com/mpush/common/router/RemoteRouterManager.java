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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.ConnectionCloseEvent;
import com.mpush.api.router.ClientLocation;
import com.mpush.api.router.RouterManager;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.api.spi.common.CacheManagerFactory;
import com.mpush.common.CacheKeys;
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

    private final CacheManager cacheManager = CacheManagerFactory.create();

    @Override
    public RemoteRouter register(String userId, RemoteRouter router) {
        String key = CacheKeys.getUserRouteKey(userId);
        String field = Integer.toString(router.getRouteValue().getClientType());
        ClientLocation old = cacheManager.hget(key, field, ClientLocation.class);
        cacheManager.hset(key, field, router.getRouteValue().toJson());
        LOGGER.info("register remote router success userId={}, newRouter={}, oldRoute={}", userId, router, old);
        return old == null ? null : new RemoteRouter(old);
    }

    /**
     * 目前的实现方式是非原子操作(get:set)，可能会有并发问题，虽然概率很低
     * 后续考虑采用lua脚本，实现原子操作
     *
     * @param userId     用户ID
     * @param clientType 客户端类型
     * @return 删除路由是否成功
     */
    @Override
    public boolean unRegister(String userId, int clientType) {
        String key = CacheKeys.getUserRouteKey(userId);
        String field = Integer.toString(clientType);
        ClientLocation location = cacheManager.hget(key, field, ClientLocation.class);
        if (location == null || location.isOffline()) return true;
        cacheManager.hset(key, field, location.offline().toJson());
        LOGGER.info("unRegister remote router success userId={}, route={}", userId, location);
        return true;
    }

    @Override
    public Set<RemoteRouter> lookupAll(String userId) {
        String key = CacheKeys.getUserRouteKey(userId);
        Map<String, ClientLocation> values = cacheManager.hgetAll(key, ClientLocation.class);
        if (values == null || values.isEmpty()) return Collections.emptySet();
        return values.values().stream().map(RemoteRouter::new).collect(Collectors.toSet());
    }

    @Override
    public RemoteRouter lookup(String userId, int clientType) {
        String key = CacheKeys.getUserRouteKey(userId);
        String field = Integer.toString(clientType);
        ClientLocation location = cacheManager.hget(key, field, ClientLocation.class);
        LOGGER.info("lookup remote router userId={}, router={}", userId, location);
        return location == null ? null : new RemoteRouter(location);
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
        String key = CacheKeys.getUserRouteKey(userId);
        String field = Integer.toString(context.getClientType());
        ClientLocation location = cacheManager.hget(key, field, ClientLocation.class);
        if (location == null || location.isOffline()) return;

        String connId = connection.getId();
        //2.检测下，是否是同一个链接, 如果客户端重连，老的路由会被新的链接覆盖
        if (connId.equals(location.getConnId())) {
            cacheManager.hset(key, field, location.offline().toJson());
            LOGGER.info("clean disconnected remote route, userId={}, route={}", userId, location);
        } else {
            LOGGER.info("clean disconnected remote route, not clean:userId={}, route={}", userId, location);
        }
    }
}
