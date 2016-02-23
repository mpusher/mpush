package com.shinemo.mpush.core.router;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.event.RouterChangeEvent;
import com.shinemo.mpush.api.router.Router;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.common.EventBus;
import com.shinemo.mpush.common.router.RemoteRouter;
import com.shinemo.mpush.common.router.RemoteRouterManager;
import com.shinemo.mpush.tools.MPushUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 */
public final class RouterCenter {
    public static final Logger LOGGER = LoggerFactory.getLogger(RouterCenter.class);
    public static final RouterCenter INSTANCE = new RouterCenter();

    private final LocalRouterManager localRouterManager = new LocalRouterManager();
    private final RemoteRouterManager remoteRouterManager = new RemoteRouterManager();
    private final RouterChangeListener routerChangeListener = new RouterChangeListener();
    private final UserManager userManager = new UserManager();
    

    /**
     * 注册用户和链接
     *
     * @param userId
     * @param connection
     * @return
     */
    public boolean register(String userId, Connection connection) {
        ClientLocation location = ClientLocation
                .from(connection.getSessionContext())
                .setHost(MPushUtil.getLocalIp());

        LocalRouter localRouter = new LocalRouter(connection);
        RemoteRouter remoteRouter = new RemoteRouter(location);

        LocalRouter oldLocalRouter = null;
        RemoteRouter oldRemoteRouter = null;
        try {
            oldLocalRouter = localRouterManager.register(userId, localRouter);
            oldRemoteRouter = remoteRouterManager.register(userId, remoteRouter);
            
        } catch (Exception e) {
            LOGGER.error("register router ex, userId={}, connection={}", userId, connection, e);
        }

        if (oldLocalRouter != null) {
            EventBus.INSTANCE.post(new RouterChangeEvent(userId, oldLocalRouter));
            LOGGER.info("register router success, find old local router={}, userId={}", oldLocalRouter, userId);
        }

        if (oldRemoteRouter != null) {
            EventBus.INSTANCE.post(new RouterChangeEvent(userId, oldRemoteRouter));
            LOGGER.info("register router success, find old remote router={}, userId={}", oldRemoteRouter, userId);
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
    
    public UserManager getUserManager(){
    	return userManager;
    }
}
