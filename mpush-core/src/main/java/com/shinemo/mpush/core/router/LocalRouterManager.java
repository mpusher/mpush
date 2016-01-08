package com.shinemo.mpush.core.router;

import com.shinemo.mpush.api.router.RouterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/23.
 */
public final class LocalRouterManager implements RouterManager<LocalRouter> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LocalRouterManager.class);
    private final Map<String, LocalRouter> routerMap = new ConcurrentHashMap<>();

    @Override
    public LocalRouter register(String userId, LocalRouter router) {
        LOGGER.debug("register local router success userId={}, router={}", userId, router);
        return routerMap.put(userId, router);
    }

    @Override
    public boolean unRegister(String userId) {
        LocalRouter router = routerMap.remove(userId);
        LOGGER.info("unRegister local router success userId={}, router={}", userId, router);
        return true;
    }

    @Override
    public LocalRouter lookup(String userId) {
        LocalRouter router = routerMap.get(userId);
        LOGGER.debug("lookup local router userId={}, router={}", userId, router);
        return router;
    }
}
