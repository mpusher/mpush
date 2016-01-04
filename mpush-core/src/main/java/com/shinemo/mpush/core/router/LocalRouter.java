package com.shinemo.mpush.core.router;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.router.Router;

/**
 * Created by ohun on 2015/12/23.
 */
public class LocalRouter implements Router<Connection> {
    private final Connection connection;

    public LocalRouter(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getRouteValue() {
        return connection;
    }

    @Override
    public RouterType getRouteType() {
        return RouterType.LOCAL;
    }
}
