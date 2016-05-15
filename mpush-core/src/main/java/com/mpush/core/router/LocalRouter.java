package com.mpush.core.router;

import com.mpush.api.connection.Connection;
import com.mpush.api.router.Router;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class LocalRouter implements Router<Connection> {
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

    @Override
    public String toString() {
        return "LocalRouter{" + connection + '}';
    }
}
