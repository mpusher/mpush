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
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import com.mpush.zk.node.ZKServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import static com.mpush.tools.config.CC.mp.net.gateway_server_multicast;
import static com.mpush.tools.config.CC.mp.net.gateway_server_port;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class GatewayUDPConnectionFactory extends GatewayConnectionFactory<InetSocketAddress> {

    private final Logger logger = LoggerFactory.getLogger(GatewayUDPConnectionFactory.class);

    private final Map<String, InetSocketAddress> ip_address = Maps.newConcurrentMap();

    private final GatewayUDPConnector connector = new GatewayUDPConnector();

    private final InetSocketAddress multicastRecipient = new InetSocketAddress(gateway_server_multicast, gateway_server_port);

    @Override
    public void init() {
        super.init();
        ThreadPoolManager.I.newThread("udp-client", connector::start).start();
    }

    @Override
    public void put(String fullPath, ZKServerNode node) {
        super.put(fullPath, node);
        ip_address.put(node.getIp(), new InetSocketAddress(node.getIp(), node.getPort()));
    }

    @Override
    public ZKServerNode remove(String fullPath) {
        ZKServerNode node = super.remove(fullPath);
        logger.warn("Gateway Server zkNode={} was removed.", node);
        return node;
    }

    @Override
    public void clear() {
        super.clear();
        ip_address.clear();
    }

    @Override
    public Connection getConnection(String ip) {
        return connector.getConnection();
    }

    @Override
    public InetSocketAddress getNode(String ip) {
        return ip_address.get(ip);
    }

    @Override
    public Collection<InetSocketAddress> getAllNode() {
        return ip_address.values();
    }

    @Override
    public boolean send(String host, Consumer<GatewayPushMessage> consumer) {
        InetSocketAddress recipient = ip_address.get(host);
        if (recipient == null) return false;
        consumer.accept(GatewayPushMessage.build(connector.getConnection(), recipient));
        return true;
    }

    @Override
    public void broadcast(Consumer<GatewayPushMessage> consumer) {
        consumer.accept(GatewayPushMessage.build(connector.getConnection(), multicastRecipient));
    }
}
