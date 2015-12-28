package com.shinemo.mpush.core;


import com.shinemo.mpush.api.Connection;

import io.netty.channel.Channel;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by ohun on 2015/12/22.
 */
public class ConnectionManager {
    public static final ConnectionManager INSTANCE = new ConnectionManager();

    //可能会有20w的链接数
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMapV8<String, Connection>();

    public Connection get(final String channelId) throws ExecutionException {
        return connections.get(channelId);
    }

    public Connection get(final Channel channel) {
        return connections.get(channel.id().asLongText());
    }

    public void add(Connection connection) {
        connections.putIfAbsent(connection.getId(), connection);
    }

    public void add(Channel channel) {
        Connection connection = new NettyConnection();
        connection.init(channel);
        connections.putIfAbsent(connection.getId(), connection);
    }

    public void remove(Connection connection) {
        connections.remove(connection.getId());
    }

    public void remove(Channel channel) {
        connections.remove(channel.id().asLongText());
    }

    public List<String> getConnectionIds() {
        return new ArrayList<String>(connections.keySet());
    }

    public List<Connection> getConnections() {
        return new ArrayList<Connection>(connections.values());
    }

}
