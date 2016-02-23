package com.shinemo.mpush.netty.connection;


import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.ConnectionManager;
import com.shinemo.mpush.api.event.HandshakeEvent;
import com.shinemo.mpush.api.event.UserOfflineEvent;
import com.shinemo.mpush.api.event.UserOnlineEvent;
import com.shinemo.mpush.common.EventBus;
import com.shinemo.mpush.log.LogType;
import com.shinemo.mpush.log.LoggerManage;
import com.shinemo.mpush.tools.config.ConfigCenter;

import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 * Created by ohun on 2015/12/22.
 */
public final class NettyConnectionManager implements ConnectionManager {
    //可能会有20w的链接数
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMapV8<>();

    private Timer timer;

    @Override
    public void init() {
        //每秒钟走一步，一个心跳周期内走一圈
        long tickDuration = 1000;//1s
        int ticksPerWheel = (int) (ConfigCenter.holder.maxHeartbeat() / tickDuration);
        this.timer = new HashedWheelTimer(tickDuration, TimeUnit.MILLISECONDS, ticksPerWheel);
        EventBus.INSTANCE.register(this);
    }

    @Override
    public void destroy() {
        if (timer != null) timer.stop();
        for (Connection connection : connections.values()) {
            connection.close();
        }
        connections.clear();
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

    @Subscribe
    void onHandshakeOk(HandshakeEvent event) {
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
            timer.newTimeout(this, heartbeat, TimeUnit.MILLISECONDS);
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            try {
                if (!connection.isConnected()) {
                    LoggerManage.info(LogType.HEARTBEAT, "connection is not connected:%s,%s", expiredTimes, connection.getChannel(), connection.getSessionContext().deviceId);
                    return;
                }
                if (connection.heartbeatTimeout()) {
                    if (++expiredTimes > ConfigCenter.holder.maxHBTimeoutTimes()) {
                    	
                    	EventBus.INSTANCE.post(new UserOfflineEvent(connection));
                    	
                        connection.close();
                        LoggerManage.info(LogType.HEARTBEAT, "connection heartbeat timeout, connection has bean closed:%s,%s", connection.getChannel(), connection.getSessionContext().deviceId);
                        return;
                    } else {
                        LoggerManage.info(LogType.HEARTBEAT, "connection heartbeat timeout, expiredTimes:%s,%s,%s", expiredTimes, connection.getChannel(), connection.getSessionContext().deviceId);
                    }
                } else {
                    expiredTimes = 0;
                    LoggerManage.info(LogType.HEARTBEAT, "connection heartbeat reset, expiredTimes:%s,%s,%s", expiredTimes, connection.getChannel(), connection.getSessionContext().deviceId);
                }
            } catch (Throwable e) {
                LoggerManage.execption(LogType.DEFAULT, e, "HeartbeatCheckTask error");
            }
            startTimeout();
        }
    }

}
