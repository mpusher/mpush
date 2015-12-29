package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.RouterManager;

/**
 * Created by ohun on 2015/12/23.
 */
public class RemoteRouterManager implements RouterManager<RemoteRouter> {

    @Override
    public RemoteRouter register(String userId, RemoteRouter route) {
        return null;
    }

    @Override
    public boolean unRegister(String userId) {
        return true;
    }

    @Override
    public RemoteRouter getRouter(String userId) {
        return null;
    }
}
