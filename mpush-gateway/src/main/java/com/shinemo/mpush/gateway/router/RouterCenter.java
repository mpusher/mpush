package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.UserConnConfig;
import com.shinemo.mpush.api.SessionContext;
import com.shinemo.mpush.api.message.KickUserMessage;

/**
 * Created by ohun on 2015/12/23.
 */
public class RouterCenter {
    public static final RouterCenter INSTANCE = new RouterCenter();

    private final LocalRouterManager localRouterManager = new LocalRouterManager();
    private final RemoteRouterManager remoteRouterManager = new RemoteRouterManager();

    public boolean register(String userId, Connection connection) {
        LocalRouter oldLocalRouter = localRouterManager.register(userId, new LocalRouter(connection));
        RemoteRouter oldRemoteRouter = remoteRouterManager.register(userId, new RemoteRouter(new UserConnConfig("127.0.0.1")));
        if (oldLocalRouter != null) {
            kickLocalUser(userId, oldLocalRouter);
        }

        if (oldRemoteRouter != null) {
            kickRemoteUser(userId, oldRemoteRouter);
        }
        return true;
    }

    public boolean unRegister(String userId) {
        localRouterManager.unRegister(userId);
        remoteRouterManager.unRegister(userId);
        return true;
    }

    public Router<?> lookup(String userId) {
        LocalRouter local = localRouterManager.getRouter(userId);
        if (local != null) return local;
        RemoteRouter remote = remoteRouterManager.getRouter(userId);
        return remote;
    }

    private void kickLocalUser(String userId, LocalRouter router) {
        Connection connection = router.getRouteInfo();
        SessionContext context = connection.getSessionContext();
        KickUserMessage message = new KickUserMessage(connection);
        message.deviceId = context.deviceId;
        message.userId = userId;
        message.send();
    }

    private void kickRemoteUser(String userId, RemoteRouter router) {
        //send msg to zk
    }
}
