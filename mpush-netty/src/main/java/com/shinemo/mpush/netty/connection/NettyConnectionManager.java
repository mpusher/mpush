package com.shinemo.mpush.netty.connection;


import com.google.common.eventbus.Subscribe;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.ConnectionManager;
import com.shinemo.mpush.api.event.HandshakeEvent;
import com.shinemo.mpush.common.EventBus;
import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 2015/12/22.
 */
public final class NettyConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnectionManager.class);

    public void registerEventBus() {
        EventBus.INSTANCE.register(this);
    }

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
        connection.init(channel, true);
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


    @Subscribe
    public void onHandshakeSuccess(HandshakeEvent event) {
        int r = event.heartbeat + 3000;
        int w = event.heartbeat + 3000;
        Channel channel = event.connection.channel();
        channel.pipeline().addFirst(new IdleStateHandler(r, w, 0, TimeUnit.MILLISECONDS));
        LOGGER.warn("NettyChannel setHeartbeat readTimeout={}, writeTimeout={}", r, w);
    }
}
