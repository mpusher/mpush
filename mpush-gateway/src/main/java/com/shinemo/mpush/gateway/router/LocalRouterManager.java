package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.RouterManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/23.
 */
public class LocalRouterManager implements RouterManager {
    private final Map<Long, Router> routerMap = new ConcurrentHashMap<Long, Router>();

    public boolean publish(long userId, Router route) {
        routerMap.put(userId, route);
        return true;
    }

    public boolean unPublish(long userId) {
        routerMap.remove(userId);
        return true;
    }

    public Router getRouter(long userId) {
        return routerMap.get(userId);
    }
}
