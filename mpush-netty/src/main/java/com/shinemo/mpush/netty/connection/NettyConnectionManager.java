package com.shinemo.mpush.netty.connection;


import com.google.common.collect.Lists;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.ConnectionManager;

import io.netty.channel.Channel;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ohun on 2015/12/22.
 */
public final class NettyConnectionManager implements ConnectionManager {
    //可能会有20w的链接数
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMapV8<>();

    @Override
    public void init() {
    }

    @Override
    public Connection get(final Channel channel) {
        return connections.get(channel.id().asLongText());
    }

    @Override
    public void add(Connection connection) {
        connections.putIfAbsent(connection.getId(), connection);
    }

    @Override
    public void remove(Channel channel) {
        Connection connection = connections.remove(channel.id().asLongText());
        if (connection != null) {
            connection.close();
        }
    }
    
    @Override
    public List<Connection> getConnections() {
    	return Lists.newArrayList(connections.values());
    }

}
