package com.shinemo.mpush.common.router;

import com.shinemo.mpush.api.router.RouterManager;

/**
 * Created by ohun on 2015/12/23.
 */
public class RemoteRouterManager implements RouterManager<RemoteRouter> {
    // TODO: 2015/12/30 add local cache

    @Override
    public RemoteRouter register(String userId, RemoteRouter route) {
        return null;
    }

    @Override
    public boolean unRegister(String userId) {
        return true;
    }

    @Override
    public RemoteRouter lookup(String userId) {
        return null;
    }
}
