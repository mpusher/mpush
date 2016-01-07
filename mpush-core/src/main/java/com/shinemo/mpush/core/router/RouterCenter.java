package com.shinemo.mpush.core.router;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.event.RouterChangeEvent;
import com.shinemo.mpush.api.router.Router;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.common.EventBus;
import com.shinemo.mpush.common.router.RemoteRouter;
import com.shinemo.mpush.common.router.RemoteRouterManager;
import com.shinemo.mpush.tools.MPushUtil;

/**
 * Created by ohun on 2015/12/23.
 */
public class RouterCenter {
    public static final RouterCenter INSTANCE = new RouterCenter();

    private final LocalRouterManager localRouterManager = new LocalRouterManager();
    private final RemoteRouterManager remoteRouterManager = new RemoteRouterManager();
    private final RouterChangeListener routerChangeListener = new RouterChangeListener();

    /**
     * 注册用户和链接
     *
     * @param userId
     * @param connection
     * @return
     */
    public boolean register(String userId, Connection connection) {
        ClientLocation connConfig = ClientLocation.from(connection.getSessionContext());
        connConfig.setHost(MPushUtil.getLocalIp());
        LocalRouter localRouter = new LocalRouter(connection);
        RemoteRouter remoteRouter = new RemoteRouter(connConfig);

        LocalRouter oldLocalRouter = localRouterManager.register(userId, localRouter);
        RemoteRouter oldRemoteRouter = remoteRouterManager.register(userId, remoteRouter);
        if (oldLocalRouter != null) {
            EventBus.INSTANCE.post(new RouterChangeEvent(userId, oldLocalRouter));
        }

        if (oldRemoteRouter != null) {
            EventBus.INSTANCE.post(new RouterChangeEvent(userId, oldRemoteRouter));
        }
        return true;
    }

    public boolean unRegister(String userId) {
        localRouterManager.unRegister(userId);
        remoteRouterManager.unRegister(userId);
        return true;
    }

    public Router<?> lookup(String userId) {
        LocalRouter local = localRouterManager.lookup(userId);
        if (local != null) return local;
        RemoteRouter remote = remoteRouterManager.lookup(userId);
        return remote;
    }


    public LocalRouterManager getLocalRouterManager() {
        return localRouterManager;
    }

    public RemoteRouterManager getRemoteRouterManager() {
        return remoteRouterManager;
    }

    public RouterChangeListener getRouterChangeListener() {
        return routerChangeListener;
    }
}
