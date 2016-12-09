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
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.ConnectionManager;
import com.mpush.api.event.HandshakeEvent;
import com.mpush.tools.config.CC;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.log.Logs;
import com.mpush.tools.thread.NamedThreadFactory;
import com.mpush.tools.thread.ThreadNames;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn
 */
public final class ServerConnectionManager implements ConnectionManager {
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();

    private HashedWheelTimer timer;
    private final boolean heartbeatCheck;

    public ServerConnectionManager(boolean heartbeatCheck) {
        this.heartbeatCheck = heartbeatCheck;
    }

    @Override
    public void init() {
        if (heartbeatCheck) {
            EventBus.I.register(this);
            long tickDuration = TimeUnit.SECONDS.toMillis(1);//1s 每秒钟走一步，一个心跳周期内大致走一圈
            int ticksPerWheel = (int) (CC.mp.core.max_heartbeat / tickDuration);
            this.timer = new HashedWheelTimer(
                    new NamedThreadFactory(ThreadNames.T_CONN_TIMER),
                    tickDuration, TimeUnit.MILLISECONDS, ticksPerWheel
            );
        }
    }

    @Override
    public void destroy() {
        if (timer != null) timer.stop();
        connections.values().forEach(Connection::close);
        connections.clear();
    }

    @Override
    public Connection get(final Channel channel) {
        return connections.get(channel.id().asLongText());
    }

    @Override
    public void add(Connection connection) {
        connections.putIfAbsent(connection.getChannel().id().asLongText(), connection);
        if (heartbeatCheck) new HeartbeatCheckTask(connection).startTimeout();
    }

    @Override
    public Connection removeAndClose(Channel channel) {
        Connection connection = connections.remove(channel.id().asLongText());
        if (connection != null) {
            connection.close();
        }
        return connection;
    }

    @Override
    public List<Connection> getConnections() {
        return Lists.newArrayList(connections.values());
    }

    //@Subscribe
    void on(HandshakeEvent event) {
        new HeartbeatCheckTask(event.connection).startTimeout();
    }

    private class HeartbeatCheckTask implements TimerTask {

        private int timeoutTimes = 0;
        private final Connection connection;

        HeartbeatCheckTask(Connection connection) {
            this.connection = connection;
        }

        void startTimeout() {
            int timeout = connection.getSessionContext().heartbeat;
            timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (!connection.isConnected()) {
                Logs.HB.info("connection was disconnected, heartbeat timeout times={}, connection={}", timeoutTimes, connection);
                return;
            }
            if (connection.isReadTimeout()) {
                if (++timeoutTimes > CC.mp.core.max_hb_timeout_times) {
                    connection.close();
                    Logs.HB.warn("client heartbeat timeout times={}, do close connection={}", timeoutTimes, connection);
                    return;
                } else {
                    Logs.HB.info("client heartbeat timeout times={}, connection={}", timeoutTimes, connection);
                }
            } else {
                timeoutTimes = 0;
            }
            startTimeout();
        }
    }
}
