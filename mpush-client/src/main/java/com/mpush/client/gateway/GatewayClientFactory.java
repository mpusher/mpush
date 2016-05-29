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

package com.mpush.client.gateway;

import com.google.common.collect.Maps;
import com.mpush.api.connection.Connection;
import com.mpush.api.service.Client;
import com.mpush.api.service.Listener;
import com.mpush.zk.cache.ZKServerNodeCache;
import com.mpush.zk.node.ZKServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class GatewayClientFactory extends ZKServerNodeCache {
    public static final GatewayClientFactory I = new GatewayClientFactory();

    private final Logger logger = LoggerFactory.getLogger(GatewayClientFactory.class);

    private final Map<String, GatewayClient> ip_client = Maps.newConcurrentMap();

    @Override
    public void put(String fullPath, ZKServerNode node) {
        super.put(fullPath, node);
        addClient(node.getIp(), node.getPort());
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
        for (GatewayClient client : ip_client.values()) {
            client.stop(null);
        }
    }

    public GatewayClient getClient(String ip) {
        GatewayClient client = ip_client.get(ip);
        if (client == null) {
            return null;//TODO create client
        }
        return client;
    }

    public Connection getConnection(String ip) {
        GatewayClient client = ip_client.get(ip);
        if (client == null) {
            return null;//TODO create client
        }
        Connection connection = client.getConnection();
        if (connection.isConnected()) {
            return connection;
        }
        restartClient(client);
        return null;
    }

    private void restartClient(final GatewayClient client) {
        ip_client.remove(client.getHost());
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
            Client client = ip_client.remove(node.getIp());
            if (client != null) {
                client.stop(null);
            }
        }
    }

    private void addClient(final String host, final int port) {
        final GatewayClient client = new GatewayClient(host, port);
        client.start(new Listener() {
            @Override
            public void onSuccess(Object... args) {
                ip_client.put(host, client);
            }

            @Override
            public void onFailure(Throwable cause) {
                logger.error("create gateway client ex, client={}", client, cause);
            }
        });
    }
}
