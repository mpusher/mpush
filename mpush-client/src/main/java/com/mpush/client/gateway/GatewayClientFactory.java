package com.mpush.client.gateway;

import com.google.common.collect.Maps;
import com.mpush.api.Client;
import com.mpush.api.Service;
import com.mpush.api.connection.Connection;
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
        addClient(node);
    }

    @Override
    public ZKServerNode remove(String fullPath) {
        ZKServerNode node = super.remove(fullPath);
        removeClient(node);
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
        return client.getConnection();
    }

    private void removeClient(ZKServerNode node) {
        if (node != null) {
            Client client = ip_client.remove(node.getIp());
            if (client != null) {
                client.stop(null);
            }
        }
    }

    private void addClient(final ZKServerNode node) {
        final GatewayClient client = new GatewayClient(node.getIp(), node.getPort());
        client.start(new Service.Listener() {
            @Override
            public void onSuccess(Object... args) {
                ip_client.put(node.getIp(), client);
            }

            @Override
            public void onFailure(Throwable cause) {
                logger.error("create gateway client ex, node", node, cause);
            }
        });
    }
}
