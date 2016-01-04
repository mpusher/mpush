package com.shinemo.mpush.netty.connection;


import com.google.common.eventbus.Subscribe;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.ConnectionManager;
import com.shinemo.mpush.api.event.HandshakeEvent;
import com.shinemo.mpush.common.EventBus;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
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
    private Timer wheelTimer;


    public void init() {
        long tickDuration = 1000;//1s
        int ticksPerWheel = (int) (Constants.HEARTBEAT_TIME / tickDuration);
        this.wheelTimer = new HashedWheelTimer(tickDuration, TimeUnit.MILLISECONDS, ticksPerWheel);
        EventBus.INSTANCE.register(this);
    }

    //可能会有20w的链接数
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMapV8<>();

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
        return new ArrayList<>(connections.keySet());
    }

    public List<Connection> getConnections() {
        return new ArrayList<>(connections.values());
    }

    @Subscribe
    public void onHandshakeOk(HandshakeEvent event) {
        HeartbeatCheckTask task = new HeartbeatCheckTask(event.heartbeat, event.connection);
        task.startTimeout();
    }

    private class HeartbeatCheckTask implements TimerTask {
        private int expiredTimes = 0;
        private final int heartbeat;
        private final Connection connection;

        public HeartbeatCheckTask(int heartbeat, Connection connection) {
            this.heartbeat = heartbeat;
            this.connection = connection;
        }

        public void startTimeout() {
            wheelTimer.newTimeout(this, heartbeat, TimeUnit.MILLISECONDS);
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (connection.heartbeatTimeout()) {
                if (++expiredTimes > 5) {
                    connection.close();
                    return;
                } else {
                    LOGGER.error("connection heartbeat timeout, expiredTimes=" + expiredTimes);
                }
            }
            startTimeout();
        }
    }
}
