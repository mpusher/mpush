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

//    private Timer wheelTimer;

    @Override
    public void init() {
//        //每秒钟走一步，一个心跳周期内走一圈
//        long tickDuration = 1000;//1s
//        int ticksPerWheel = (int) (ConfigCenter.holder.maxHeartbeat() / tickDuration);
//        this.wheelTimer = new HashedWheelTimer(tickDuration, TimeUnit.MILLISECONDS, ticksPerWheel);
//        EventBus.INSTANCE.register(this);
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

//    @Subscribe
//    void onHandshakeOk(HandshakeEvent event) {
//        HeartbeatCheckTask task = new HeartbeatCheckTask(event.heartbeat, event.connection);
//        task.startTimeout();
//    }
//
//    private class HeartbeatCheckTask implements TimerTask {
//        private int expiredTimes = 0;
//        private final int heartbeat;
//        private final Connection connection;
//
//        public HeartbeatCheckTask(int heartbeat, Connection connection) {
//            this.heartbeat = heartbeat;
//            this.connection = connection;
//            LOGGER.warn("heart-beat-check-task:"+Thread.currentThread().getName()+",start");
//        }
//
//        public void startTimeout() {
//            wheelTimer.newTimeout(this, heartbeat, TimeUnit.MILLISECONDS);
//        }
//
//        @Override
//        public void run(Timeout timeout) throws Exception {
//            if (!connection.isConnected()) return;
//            if (connection.heartbeatTimeout()) {
//                if (++expiredTimes > ConfigCenter.holder.maxHBTimeoutTimes()) {
//                    connection.close();
//                    LOGGER.error("connection heartbeat timeout, connection has bean closed");
//                    return;
//                } else {
//                    LOGGER.error("connection heartbeat timeout, expiredTimes=" + expiredTimes);
//                }
//            } else {
//                expiredTimes = 0;
//                //LOGGER.info("check heartbeat timeout");
//            }
//            startTimeout();
//        }
//    }
}
