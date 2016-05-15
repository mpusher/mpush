package com.mpush.push.zk.manager;

import com.google.common.collect.Maps;
import com.mpush.netty.client.NettyClientFactory;
import com.mpush.zk.ZKNodeManager;
import com.mpush.zk.ZKServerNode;
import com.mpush.api.Client;
import com.mpush.api.connection.Connection;
import com.mpush.netty.client.NettyClient;
import com.mpush.push.client.ClientChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class GatewayZKNodeManager implements ZKNodeManager<ZKServerNode> {

    private final Logger log = LoggerFactory.getLogger(GatewayZKNodeManager.class);

    private static Map<String, ZKServerNode> holder = Maps.newConcurrentMap();

    private final Map<ZKServerNode, Client> application2Client = Maps.newConcurrentMap();

    private final Map<String, Client> ip2Client = Maps.newConcurrentMap();

    @Override
    public void addOrUpdate(String fullPath, ZKServerNode application) {
        holder.put(fullPath, application);
        try {
            Client client = new NettyClient(application.getIp(), application.getPort());
            ClientChannelHandler handler = new ClientChannelHandler(client);
            NettyClientFactory.INSTANCE.create(handler);
            application2Client.put(application, client);
            ip2Client.put(application.getIp(), client);
        } catch (Exception e) {
            log.error("addOrUpdate:{},{}", fullPath, application, e);
        }
        printList();
    }

    @Override
    public void remove(String fullPath) {
        ZKServerNode application = get(fullPath);
        if (application != null) {
            Client client = application2Client.get(application);
            if (client != null) {
                client.stop();
            }
        }
        ip2Client.remove(application.getIp() + ":" + application.getPort());
        holder.remove(fullPath);
        printList();
    }

    @Override
    public Collection<ZKServerNode> getList() {
        return Collections.unmodifiableCollection(holder.values());
    }

    private void printList() {
        for (ZKServerNode app : holder.values()) {
            log.warn(app.toString());
        }
    }

    public ZKServerNode get(String fullpath) {
        return holder.get(fullpath);
    }

    public Client getClient(ZKServerNode application) {
        return application2Client.get(application);
    }

    public Connection getConnection(String ipAndPort) {
        Client client = ip2Client.get(ipAndPort);
        if (client == null) return null;
        return client.getConnection();
    }

}

