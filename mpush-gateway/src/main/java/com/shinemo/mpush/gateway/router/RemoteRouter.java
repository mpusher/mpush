package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.UserConnConfig;

/**
 * Created by ohun on 2015/12/23.
 */
public class RemoteRouter implements Router<UserConnConfig> {
    private final UserConnConfig userConnConfig;

    public RemoteRouter(UserConnConfig userConnConfig) {
        this.userConnConfig = userConnConfig;
    }

    @Override
    public UserConnConfig getRouteInfo() {
        return userConnConfig;
    }

    @Override
    public RouterType getType() {
        return RouterType.REMOTE;
    }
}
