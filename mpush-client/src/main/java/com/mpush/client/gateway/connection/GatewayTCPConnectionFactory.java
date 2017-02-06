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
import com.mpush.api.spi.common.ServiceDiscoveryFactory;
import com.mpush.api.srd.ServiceDiscovery;
import com.mpush.api.srd.ServiceNode;
import com.mpush.client.gateway.GatewayClient;
import com.mpush.common.message.BaseMessage;
import com.mpush.tools.event.EventBus;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mpush.api.srd.ServiceNames.GATEWAY_SERVER;
import static com.mpush.tools.config.CC.mp.net.gateway_client_num;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class GatewayTCPConnectionFactory extends GatewayConnectionFactory {
    private final AttributeKey<String> attrKey = AttributeKey.valueOf("host_port");
    private final Map<String, List<Connection>> connections = Maps.newConcurrentMap();

    private GatewayClient client;

    @Override
    protected void doStart(Listener listener) throws Throwable {
        EventBus.I.register(this);

        client = new GatewayClient();
        client.start().join();

        ServiceDiscovery discovery = ServiceDiscoveryFactory.create();
        discovery.subscribe(GATEWAY_SERVER, this);
        discovery.lookup(GATEWAY_SERVER).forEach(this::addConnection);
        listener.onSuccess();
    }

    @Override
    public void onServiceAdded(String path, ServiceNode node) {
        asyncAddConnection(node);
    }

    @Override
    public void onServiceUpdated(String path, ServiceNode node) {
        removeClient(node);
        asyncAddConnection(node);
    }

    @Override
    public void onServiceRemoved(String path, ServiceNode node) {
        removeClient(node);
        logger.warn("Gateway Server zkNode={} was removed.", node);
    }

    @Override
    public void doStop(Listener listener) throws Throwable {
        connections.values().forEach(l -> l.forEach(Connection::close));
        if (client != null) {
            client.stop().join();
        }
        ServiceDiscoveryFactory.create().unsubscribe(GATEWAY_SERVER, this);
    }

    @Override
    public Connection getConnection(String hostAndPort) {
        List<Connection> connections = this.connections.get(hostAndPort);
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
        return getConnection(hostAndPort);
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
        if (connections.isEmpty()) return false;
        connections
                .values()
                .stream()
                .filter(connections -> connections.size() > 0)
                .forEach(connections -> sender.accept(creator.apply(connections.get(0))));
        return true;
    }

    private void reconnect(Connection connection, String hostAndPort) {
        HostAndPort h_p = HostAndPort.fromString(hostAndPort);
        connections.get(hostAndPort).remove(connection);
        connection.close();
        addConnection(h_p.getHost(), h_p.getPort(), false);
    }

    private void removeClient(ServiceNode node) {
        if (node != null) {
            List<Connection> clients = connections.remove(getHostAndPort(node.getHost(), node.getPort()));
            if (clients != null) {
                clients.forEach(Connection::close);
            }
        }
    }

    private void asyncAddConnection(ServiceNode node) {
        for (int i = 0; i < gateway_client_num; i++) {
            addConnection(node.getHost(), node.getPort(), false);
        }
    }

    private void addConnection(ServiceNode node) {
        for (int i = 0; i < gateway_client_num; i++) {
            addConnection(node.getHost(), node.getPort(), true);
        }
    }

    private void addConnection(String host, int port, boolean sync) {
        ChannelFuture future = client.connect(host, port);
        future.channel().attr(attrKey).set(getHostAndPort(host, port));
        future.addListener(f -> {
            if (!f.isSuccess()) {
                logger.error("create gateway connection ex, host={}, port={}", host, port, f.cause());
            }
        });
        if (sync) future.awaitUninterruptibly();
    }

    @Subscribe
    void on(ConnectionConnectEvent event) {
        Connection connection = event.connection;
        String hostAndPort = connection.getChannel().attr(attrKey).getAndSet(null);
        if (hostAndPort == null) {
            InetSocketAddress address = (InetSocketAddress) connection.getChannel().remoteAddress();
            hostAndPort = getHostAndPort(address.getAddress().getHostAddress(), address.getPort());
        }
        connections.computeIfAbsent(hostAndPort, key -> new ArrayList<>(gateway_client_num)).add(connection);
        logger.info("one gateway client connect success, hostAndPort={}, conn={}", hostAndPort, connection);
    }

    private static String getHostAndPort(String host, int port) {
        return host + ":" + port;
    }
}
