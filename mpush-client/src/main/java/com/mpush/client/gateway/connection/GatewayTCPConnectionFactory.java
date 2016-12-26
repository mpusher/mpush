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
import com.mpush.api.connection.Connection;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Client;
import com.mpush.api.service.Listener;
import com.mpush.client.gateway.GatewayClient;
import com.mpush.common.message.BaseMessage;
import com.mpush.tools.config.CC;
import com.mpush.zk.node.ZKServerNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class GatewayTCPConnectionFactory extends GatewayConnectionFactory {

    private final int connNum = CC.mp.net.gateway_client_num;

    private final Map<String, List<GatewayClient>> ip_client = Maps.newConcurrentMap();

    @Override
    public void put(String fullPath, ZKServerNode node) {
        super.put(fullPath, node);
        for (int i = 0; i < connNum; i++) {
            addClient(node.getIp(), node.getPort());
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
        ip_client.values().forEach(l -> l.forEach(GatewayClient::stop));
    }

    @Override
    public Connection getConnection(String hostAndPort) {
        List<GatewayClient> clients = ip_client.get(hostAndPort);
        if (clients == null || clients.isEmpty()) {
            return null;//TODO create client
        }
        int L = clients.size();
        GatewayClient client;
        if (L == 1) {
            client = clients.get(0);
        } else {
            client = clients.get((int) (Math.random() * L % L));
        }
        Connection connection = client.getConnection();
        if (connection.isConnected()) {
            return connection;
        }
        restartClient(client);
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
        ip_client.forEach((s, clients) -> sender.accept(creator.apply(clients.get(0).getConnection())));
        return true;
    }

    private void restartClient(final GatewayClient client) {
        ip_client.get(client.getHostAndPort()).remove(client);
        client.stop(new Listener() {
            @Override
            public void onSuccess(Object... args) {
                addClient(client.getHost(), client.getPort());
            }

            @Override
            public void onFailure(Throwable cause) {
                addClient(client.getHost(), client.getPort());
            }
        });
    }

    private void removeClient(ZKServerNode node) {
        if (node != null) {
            List<GatewayClient> clients = ip_client.remove(node.getHostAndPort());
            if (clients != null) {
                clients.forEach(GatewayClient::stop);
            }
        }
    }

    private void addClient(final String host, final int port) {
        final GatewayClient client = new GatewayClient(host, port);
        client.start(new Listener() {
            @Override
            public void onSuccess(Object... args) {
                ip_client.computeIfAbsent(client.getHostAndPort(), s -> new ArrayList<>(connNum)).add(client);
            }

            @Override
            public void onFailure(Throwable cause) {
                logger.error("create gateway client ex, client={}", client, cause);
            }
        });
    }
}
