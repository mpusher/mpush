package com.shinemo.mpush.core.router;

import com.shinemo.mpush.api.router.RouterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/23.
 */
public class LocalRouterManager implements RouterManager<LocalRouter> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LocalRouterManager.class);
    private final Map<String, LocalRouter> routerMap = new ConcurrentHashMap<>();

    @Override
    public LocalRouter register(String userId, LocalRouter route) {
        return routerMap.put(userId, route);
    }

    @Override
    public boolean unRegister(String userId) {
        LocalRouter router = routerMap.remove(userId);
        LOGGER.info("unRegister local router success userId={}, router={}", userId, router);
        return true;
    }

    @Override
    public LocalRouter lookup(String userId) {
        return routerMap.get(userId);
    }
}
