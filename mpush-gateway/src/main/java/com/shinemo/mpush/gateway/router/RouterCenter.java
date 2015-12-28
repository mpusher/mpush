package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.RouterInfo;

/**
 * Created by ohun on 2015/12/23.
 */
public class RouterCenter {
    public static final RouterCenter INSTANCE = new RouterCenter();

    private final LocalRouterManager localRouterManager = new LocalRouterManager();
    private final RemoteRouterManager remoteRouterManager = new RemoteRouterManager();

    public boolean publish(String userId, Connection connection) {
        localRouterManager.publish(userId, new LocalRouter(connection));
        remoteRouterManager.publish(userId, new RemoteRouter(new RouterInfo("127.0.0.1")));
        return true;
    }

    public boolean unPublish(String userId) {
        localRouterManager.unPublish(userId);
        remoteRouterManager.unPublish(userId);
        return true;
    }

    public Router lookup(String userId) {
        Router local = localRouterManager.getRouter(userId);
        if (local != null) return local;
        Router remote = remoteRouterManager.getRouter(userId);
        return remote;
    }
}
