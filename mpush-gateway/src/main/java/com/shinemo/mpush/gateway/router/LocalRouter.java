package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Router;

/**
 * Created by ohun on 2015/12/23.
 */
public class LocalRouter implements Router<Connection> {
    private final Connection connection;

    public LocalRouter(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getRouteInfo() {
        return connection;
    }

    @Override
    public RouterType getRouteType() {
        return RouterType.LOCAL;
    }
}
