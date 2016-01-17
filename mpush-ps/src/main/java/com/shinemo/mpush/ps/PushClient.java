//package com.shinemo.mpush.ps;
//
//import com.google.common.base.Strings;
//import com.shinemo.mpush.api.Client;
//import com.shinemo.mpush.api.PushSender;
//import com.shinemo.mpush.api.connection.Connection;
//import com.shinemo.mpush.common.Application;
//import com.shinemo.mpush.netty.client.NettyClient;
//import com.shinemo.mpush.tools.Jsons;
//import com.shinemo.mpush.tools.spi.ServiceContainer;
//import com.shinemo.mpush.tools.thread.ThreadPoolUtil;
//import com.shinemo.mpush.tools.zk.ZKPath;
//import com.shinemo.mpush.tools.zk.ZkRegister;
//
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
//import org.apache.curator.framework.recipes.cache.TreeCacheListener;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Created by ohun on 2015/12/30.
// */
//public class PushClient implements PushSender {
//
//    private int defaultTimeout = 3000;
//    private final Map<String, Client> clientMap = new ConcurrentHashMap<>();
//    private final Map<String, Application> servers = new ConcurrentHashMap<>();
//    
//    private static final ZkRegister zkRegister = ServiceContainer.getInstance(ZkRegister.class);
//
//    public void init() throws Exception {
//        initRedisClient();
//        GatewayServerZKListener listener = new GatewayServerZKListener();
//        Collection<Application> nodes = listener.getAllServers();
//        if (nodes == null || nodes.isEmpty()) return;
//        for (Application server : nodes) {
//            createClient(server.getIp(), server.getPort());
//        }
//    }
//
//    private void createClient(final String ip, int port) {
//        Client client = clientMap.get(ip);
//        if (client == null) {
//            final Client cli = new NettyClient(ip, port, new GatewayClientChannelHandler());
//            ThreadPoolUtil.newThread(new Runnable() {
//                @Override
//                public void run() {
//                    cli.init();
//                    cli.start();
//                }
//            }, "push-client-" + ip).start();
//            clientMap.put(ip, cli);
//        }
//    }
//
//    public Connection getConnection(String ip) {
//        Client client = clientMap.get(ip);
//        if (client == null) return null;
//        return ((GatewayClientChannelHandler) client.getHandler()).getConnection();
//    }
//
//    @Override
//    public void send(String content, Collection<String> userIds, Callback callback) {
//        for (String userId : userIds) {
//            PushRequest
//                    .build(this)
//                    .setCallback(callback)
//                    .setUserId(userId)
//                    .setContent(content)
//                    .setTimeout(defaultTimeout)
//                    .send();
//        }
//    }
//
//    public void initRedisClient() {
////        RedisPathListener listener = new RedisPathListener();
////        zkRegister.getCache().getListenable().addListener(listener);
////        listener.initData(null);
//    }
//
//    private class GatewayServerZKListener implements TreeCacheListener {
//
//        public GatewayServerZKListener() {
//        	zkRegister.getCache().getListenable().addListener(this);
//        }
//
//        @Override
//        public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
//            if (event.getData() == null) return;
//            String path = event.getData().getPath();
//            if (Strings.isNullOrEmpty(path)) return;
//
//            if (TreeCacheEvent.Type.NODE_ADDED == event.getType()) {
//            	Application serverApp = getServer(path);
//                if (serverApp != null) {
//                    createClient(serverApp.getIp(), serverApp.getPort());
//                    servers.put(path, serverApp);
//                }
//            } else if (TreeCacheEvent.Type.NODE_REMOVED == event.getType()) {
//            	Application serverApp = servers.remove(path);
//                if (serverApp != null) {
//                    Client client = clientMap.remove(serverApp.getIp());
//                    if (client != null) {
//                        client.stop();
//                    }
//                }
//            } else if (TreeCacheEvent.Type.NODE_UPDATED == event.getType()) {
//
//            }
//        }
//
//        private Application getServer(String path) {
//            String json = zkRegister.get(path);
//            if (Strings.isNullOrEmpty(json)) return null;
//            return Jsons.fromJson(json, Application.class);
//        }
//
//        private Collection<Application> getAllServers() {
//            List<String> list = zkRegister.getChildrenKeys(ZKPath.GATEWAY_SERVER.getPath());
//            if (list == null || list.isEmpty()) return Collections.EMPTY_LIST;
//            for (String name : list) {
//                String fullPath = ZKPath.GATEWAY_SERVER.getFullPath(name);
//                String json = zkRegister.get(fullPath);
//                if (com.shinemo.mpush.tools.Strings.isBlank(json)) continue;
//                Application server = Jsons.fromJson(json, Application.class);
//                if (server != null) {
//                    servers.put(fullPath, server);
//                }
//            }
//            return servers.values();
//        }
//    }
//}
