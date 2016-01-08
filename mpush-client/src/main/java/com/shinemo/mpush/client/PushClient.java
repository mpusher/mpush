package com.shinemo.mpush.client;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.netty.client.NettyClient;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import com.shinemo.mpush.tools.ConfigCenter;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.thread.ThreadPoolUtil;
import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZkUtil;
import com.shinemo.mpush.tools.zk.listener.impl.RedisPathListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushClient implements PushSender {

    private int defaultTimeout = 3000;
    private int port = 4000;
    private final Map<String, Client> clientMap = new ConcurrentHashMap<>();
    private final Map<String, ServerApp> servers = new ConcurrentHashMap<>();

    public void init() throws Exception {
        ConfigCenter.INSTANCE.init();
        initRedisClient();
        GatewayServerZKListener listener = new GatewayServerZKListener();
        Collection<ServerApp> nodes = listener.getAllServers();
        if (nodes == null || nodes.isEmpty()) return;
        for (ServerApp server : nodes) {
            createClient(server.getIp(), server.getPort());
        }
    }

    private void createClient(final String ip, int port) {
        Client client = clientMap.get(ip);
        if (client == null) {
            final Client cli = new NettyClient(ip, port, new PushClientChannelHandler());
            ThreadPoolUtil.newThread(new Runnable() {
                @Override
                public void run() {
                    cli.init();
                    cli.start();
                }
            }, "push-client-" + ip).start();
            clientMap.put(ip, cli);
        }
    }

    public Connection getConnection(String ip) {
        Client client = clientMap.get(ip);
        if (client == null) return null;
        return ((PushClientChannelHandler) client.getHandler()).getConnection();
    }

    @Override
    public void send(String content, Collection<String> userIds, Callback callback) {
        for (String userId : userIds) {
            System.out.println(userId);
            PushRequest
                    .build(this)
                    .setCallback(callback)
                    .setUserId(userId)
                    .setContent(content)
                    .setTimeout(defaultTimeout)
                    .send();
        }
    }

    public void initRedisClient() {
        RedisPathListener listener = new RedisPathListener();
        ZkUtil.instance.getCache().getListenable().addListener(listener);
        listener.initData(null);
    }

    private class GatewayServerZKListener implements TreeCacheListener {

        public GatewayServerZKListener() {
            ZkUtil.instance.getCache().getListenable().addListener(this);
        }

        @Override
        public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
            if (event.getData() == null) return;
            String path = event.getData().getPath();
            if (Strings.isNullOrEmpty(path)) return;

            if (TreeCacheEvent.Type.NODE_ADDED == event.getType()) {
                ServerApp serverApp = getServer(path);
                if (serverApp != null) {
                    createClient(serverApp.getIp(), serverApp.getPort());
                    servers.put(path, serverApp);
                }
            } else if (TreeCacheEvent.Type.NODE_REMOVED == event.getType()) {
                ServerApp serverApp = servers.remove(path);
                if (serverApp != null) {
                    clientMap.remove(serverApp.getIp());
                }
            } else if (TreeCacheEvent.Type.NODE_UPDATED == event.getType()) {

            }
        }

        private ServerApp getServer(String path) {
            String json = ZkUtil.instance.get(path);
            if (Strings.isNullOrEmpty(json)) return null;
            return Jsons.fromJson(json, ServerApp.class);
        }

        private Collection<ServerApp> getAllServers() {
            List<String> list = ZkUtil.instance.getChildrenKeys(ZKPath.GATEWAY_SERVER.getPath());
            if (list == null || list.isEmpty()) return Collections.EMPTY_LIST;
            for (String name : list) {
                String fullPath = ZKPath.GATEWAY_SERVER.getFullPath(name);
                String json = ZkUtil.instance.get(fullPath);
                if (com.shinemo.mpush.tools.Strings.isBlank(json)) continue;
                ServerApp server = Jsons.fromJson(json, ServerApp.class);
                if (server != null) {
                    servers.put(fullPath, server);
                }
            }
            return servers.values();
        }
    }
}
