package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.RouterManager;

/**
 * Created by ohun on 2015/12/23.
 */
public class RemoteRouterManager implements RouterManager {

    public boolean publish(long userId, Router route) {
        return true;
    }

    public boolean unPublish(long userId) {
        return true;
    }

    public Router getRouter(long userId) {
        return null;
    }
}
