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

package com.mpush.client.gateway.connection;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.HostAndPort;
import com.mpush.api.connection.Connection;
import com.mpush.api.event.ConnectionConnectEvent;
import com.mpush.api.service.Listener;
import com.mpush.client.gateway.GatewayClient;
import com.mpush.common.message.BaseMessage;
import com.mpush.tools.config.CC;
import com.mpush.tools.event.EventBus;
import com.mpush.zk.node.ZKServerNode;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mpush.tools.config.CC.mp.net.gateway_client_num;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class GatewayTCPConnectionFactory extends GatewayConnectionFactory {

    private final Map<String, List<Connection>> ip_client = Maps.newConcurrentMap();

    private GatewayClient client;

    @Override
    public void init(Listener listener) {
        EventBus.I.register(this);
        client = new GatewayClient();
        client.start().join();
        listener.onSuccess();
    }

    @Override
    public void put(String fullPath, ZKServerNode node) {
        super.put(fullPath, node);
        for (int i = 0; i < gateway_client_num; i++) {
            addConnection(node.getIp(), node.getPort());
        }
    }

    @Override
    public ZKServerNode remove(String fullPath) {
        ZKServerNode node = super.remove(fullPath);
        removeClient(node);
        logger.warn("Gateway Server zkNode={} was removed.", node);
        return node;
    }

    @Override
    public void clear() {
        super.clear();
        ip_client.values().forEach(l -> l.forEach(Connection::close));
        if (client != null) {
            client.stop();
        }
    }

    @Override
    public Connection getConnection(String hostAndPort) {
        List<Connection> connections = ip_client.get(hostAndPort);
        if (connections == null || connections.isEmpty()) {
            return null;//TODO create client
        }
        int L = connections.size();
        Connection connection;
        if (L == 1) {
            connection = connections.get(0);
        } else {
            connection = connections.get((int) (Math.random() * L % L));
        }
        if (connection.isConnected()) {
            return connection;
        }
        reconnect(connection, hostAndPort);
        return null;
    }

    @Override
    public <M extends BaseMessage> boolean send(String hostAndPort, Function<Connection, M> creator, Consumer<M> sender) {
        Connection connection = getConnection(hostAndPort);
        if (connection == null) return false;// gateway server 找不到，直接返回推送失败

        sender.accept(creator.apply(connection));
        return true;
    }

    @Override
    public <M extends BaseMessage> boolean broadcast(Function<Connection, M> creator, Consumer<M> sender) {
        if (ip_client.isEmpty()) return false;
        ip_client
                .values()
                .stream()
                .filter(connections -> connections.size() > 0)
                .forEach(connections -> sender.accept(creator.apply(connections.get(0))));
        return true;
    }

    private void reconnect(Connection connection, String hostAndPort) {
        HostAndPort h_p = HostAndPort.fromHost(hostAndPort);
        ip_client.get(getHostAndPort(h_p.getHostText(), h_p.getPort())).remove(connection);
        connection.close();
        addConnection(h_p.getHostText(), h_p.getPort());
    }

    private void removeClient(ZKServerNode node) {
        if (node != null) {
            List<Connection> clients = ip_client.remove(node.getHostAndPort());
            if (clients != null) {
                clients.forEach(Connection::close);
            }
        }
    }

    private void addConnection(String host, int port) {
        String hostAndPort = getHostAndPort(host, port);
        ChannelFuture future = client.connect(host, port);
        future.addListener(f -> {
            if (!f.isSuccess()) {
                logger.error("create gateway connection ex, hostAndPort={}", hostAndPort, f.cause());
            }
        });
    }

    @Subscribe
    void on(ConnectionConnectEvent event) {
        Connection connection = event.connection;
        InetSocketAddress address = (InetSocketAddress) connection.getChannel().remoteAddress();
        String hostAndPort = getHostAndPort(address.getHostName(), address.getPort());
        ip_client.computeIfAbsent(hostAndPort, key -> new ArrayList<>(gateway_client_num)).add(connection);
    }

    private static String getHostAndPort(String host, int port) {
        return host + ":" + port;
    }
}
