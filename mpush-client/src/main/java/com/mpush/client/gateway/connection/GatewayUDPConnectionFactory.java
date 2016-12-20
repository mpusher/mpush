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
import com.mpush.client.gateway.GatewayUDPConnector;
import com.mpush.common.message.BaseMessage;
import com.mpush.tools.common.Holder;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import com.mpush.zk.node.ZKServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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

    private final GatewayUDPConnector connector = GatewayUDPConnector.I();

    private final InetSocketAddress multicastRecipient = new InetSocketAddress(gateway_server_multicast, gateway_server_port);

    @Override
    public void init(Listener listener) {
        ThreadPoolManager.I.newThread("udp-client", () -> connector.start(listener)).start();
    }

    @Override
    public void put(String fullPath, ZKServerNode node) {
        super.put(fullPath, node);
        ip_address.put(node.getHostAndPort(), new InetSocketAddress(node.getIp(), node.getPort()));
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
        connector.stop();
    }

    @Override
    public Connection getConnection(String hostAndPort) {
        return connector.getConnection();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseMessage> void send(String hostAndPort, Function<Connection, T> creator, Function<T, Void> sender) {
        InetSocketAddress recipient = ip_address.get(hostAndPort);
        if (recipient == null) {// gateway server 找不到，直接返回推送失败
            creator.apply(null);
            return;
        }

        creator.compose(this::getConnection)
                .andThen(message -> (T) message.setRecipient(recipient))
                .andThen(sender)
                .apply(hostAndPort);
    }

    @Override
    public <M extends BaseMessage> void broadcast(Function<Connection, M> creator, Consumer<M> sender) {
        M message = creator.apply(connector.getConnection());
        message.setRecipient(multicastRecipient);
        sender.accept(message);
    }
}
