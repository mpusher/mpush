package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.RouterManager;

/**
 * Created by ohun on 2015/12/23.
 */
public class RemoteRouterManager implements RouterManager {

    @Override
    public boolean publish(String userId, Router route) {
        return true;
    }

    @Override
    public boolean unPublish(String userId) {
        return true;
    }

    @Override
    public Router getRouter(String userId) {
        return null;
    }
}
