package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.RouterManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/23.
 */
public class LocalRouterManager implements RouterManager {
    private final Map<String, Router> routerMap = new ConcurrentHashMap<>();

    @Override
    public boolean publish(String userId, Router route) {
        routerMap.put(userId, route);
        return true;
    }

    @Override
    public boolean unPublish(String userId) {
        routerMap.remove(userId);
        return true;
    }

    @Override
    public Router getRouter(String userId) {
        return routerMap.get(userId);
    }
}
