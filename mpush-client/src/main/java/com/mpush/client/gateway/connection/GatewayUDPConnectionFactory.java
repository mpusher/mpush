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
import com.mpush.api.service.Listener;
import com.mpush.api.spi.common.ServiceDiscoveryFactory;
import com.mpush.api.srd.ServiceDiscovery;
import com.mpush.api.srd.ServiceNode;
import com.mpush.client.MPushClient;
import com.mpush.client.gateway.GatewayUDPConnector;
import com.mpush.common.message.BaseMessage;
import com.mpush.tools.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mpush.api.srd.ServiceNames.GATEWAY_SERVER;
import static com.mpush.tools.config.CC.mp.net.gateway_server_multicast;
import static com.mpush.tools.config.CC.mp.net.gateway_server_port;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class GatewayUDPConnectionFactory extends GatewayConnectionFactory {

    private final Logger logger = LoggerFactory.getLogger(GatewayUDPConnectionFactory.class);

    private final Map<String, InetSocketAddress> ip_address = Maps.newConcurrentMap();

    private final InetSocketAddress multicastRecipient = new InetSocketAddress(gateway_server_multicast, gateway_server_port);

    private final GatewayUDPConnector gatewayUDPConnector;

    public GatewayUDPConnectionFactory(MPushClient mPushClient) {
        gatewayUDPConnector = new GatewayUDPConnector(mPushClient);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        Utils.newThread("udp-client", () -> gatewayUDPConnector.start(listener)).start();
        ServiceDiscovery discovery = ServiceDiscoveryFactory.create();
        discovery.subscribe(GATEWAY_SERVER, this);
        discovery.lookup(GATEWAY_SERVER).forEach(this::addConnection);
    }

    @Override
    public void onServiceAdded(String path, ServiceNode node) {
        addConnection(node);
    }

    @Override
    public void onServiceUpdated(String path, ServiceNode node) {
        addConnection(node);
    }

    @Override
    public void onServiceRemoved(String path, ServiceNode node) {
        ip_address.remove(node.hostAndPort());
        logger.warn("Gateway Server zkNode={} was removed.", node);
    }

    private void addConnection(ServiceNode node) {
        ip_address.put(node.hostAndPort(), new InetSocketAddress(node.getHost(), node.getPort()));
    }

    @Override
    public void doStop(Listener listener) throws Throwable {
        ip_address.clear();
        gatewayUDPConnector.stop();
    }

    @Override
    public Connection getConnection(String hostAndPort) {
        return gatewayUDPConnector.getConnection();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends BaseMessage> boolean send(String hostAndPort, Function<Connection, M> creator, Consumer<M> sender) {
        InetSocketAddress recipient = ip_address.get(hostAndPort);
        if (recipient == null) return false;// gateway server 找不到，直接返回推送失败

        M message = creator.apply(gatewayUDPConnector.getConnection());
        message.setRecipient(recipient);
        sender.accept(message);
        return true;
    }

    @Override
    public <M extends BaseMessage> boolean broadcast(Function<Connection, M> creator, Consumer<M> sender) {
        M message = creator.apply(gatewayUDPConnector.getConnection());
        message.setRecipient(multicastRecipient);
        sender.accept(message);
        return true;
    }

    public GatewayUDPConnector getGatewayUDPConnector() {
        return gatewayUDPConnector;
    }
}
