package com.shinemo.mpush.core;

import com.shinemo.mpush.api.protocol.Connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/22.
 */
public class ConnectionManager {
    public static final ConnectionManager INSTANCE = new ConnectionManager();
    private Map<String, Connection> connections = new ConcurrentHashMap<String, Connection>();

    public Connection get(String channelId) {
        return connections.get(channelId);
    }

    public void add(Connection connection) {
        connections.put(connection.getId(), connection);
    }

    public void remove(Connection connection) {
        connections.remove(connection.getId());
    }
}
