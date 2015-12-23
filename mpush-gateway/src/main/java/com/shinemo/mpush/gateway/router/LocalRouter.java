package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.RouterInfo;

/**
 * Created by ohun on 2015/12/23.
 */
public class LocalRouter implements Router {
    private final Connection connection;

    public LocalRouter(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnect() {
        return connection;
    }

    public RouterInfo getRouterInfo() {
        return null;
    }
}
