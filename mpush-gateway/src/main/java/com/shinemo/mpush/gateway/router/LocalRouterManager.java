package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.RouterManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/23.
 */
public class LocalRouterManager implements RouterManager<LocalRouter> {
    private final Map<String, LocalRouter> routerMap = new ConcurrentHashMap<>();

    @Override
    public LocalRouter register(String userId, LocalRouter route) {
        return routerMap.put(userId, route);
    }

    @Override
    public boolean unRegister(String userId) {
        routerMap.remove(userId);
        return true;
    }

    @Override
    public LocalRouter lookup(String userId) {
        return routerMap.get(userId);
    }
}
