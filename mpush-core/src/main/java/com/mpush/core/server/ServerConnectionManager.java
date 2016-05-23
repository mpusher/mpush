/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.core.server;


import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.ConnectionManager;
import com.mpush.api.event.HandshakeEvent;
import com.mpush.tools.config.CC;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.log.Logs;
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
 *
 * @author ohun@live.cn
 */
public final class ServerConnectionManager implements ConnectionManager {
    //可能会有20w的链接数
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMapV8<>();

    private Timer timer;

    @Override
    public void init() {
        //每秒钟走一步，一个心跳周期内走一圈
        long tickDuration = 1000;//1s
        int ticksPerWheel = (int) (CC.mp.core.max_heartbeat / tickDuration);
        this.timer = new HashedWheelTimer(tickDuration, TimeUnit.MILLISECONDS, ticksPerWheel);
        EventBus.I.register(this);
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
        HeartbeatCheckTask task = new HeartbeatCheckTask(event.connection);
        task.startTimeout();
    }

    private class HeartbeatCheckTask implements TimerTask {

        private int expiredTimes = 0;
        private final Connection connection;

        public HeartbeatCheckTask(Connection connection) {
            this.connection = connection;
        }

        public void startTimeout() {
            int timeout = connection.getSessionContext().heartbeat;
            timer.newTimeout(this, timeout > 0 ? timeout : CC.mp.core.min_heartbeat, TimeUnit.MILLISECONDS);
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (!connection.isConnected()) {
                Logs.HB.info("connection is not connected:{},{}", expiredTimes, connection.getChannel(), connection.getSessionContext().deviceId);
                return;
            }
            if (connection.heartbeatTimeout()) {
                if (++expiredTimes > CC.mp.core.max_hb_timeout_times) {
                    connection.close();
                    Logs.HB.info("connection heartbeat timeout, connection has bean closed:{},{}", connection.getChannel(), connection.getSessionContext().deviceId);
                    return;
                } else {
                    Logs.HB.info("connection heartbeat timeout, expiredTimes:{},{},{}", expiredTimes, connection.getChannel(), connection.getSessionContext().deviceId);
                }
            } else {
                expiredTimes = 0;
                Logs.HB.info("connection heartbeat reset, expiredTimes:{},{},{}", expiredTimes, connection.getChannel(), connection.getSessionContext().deviceId);
            }

            startTimeout();
        }
    }
}
