package com.shinemo.mpush.client;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import com.shinemo.mpush.tools.ConfigCenter;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.thread.ThreadPoolUtil;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZkUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushClient implements PushSender {

    public NettyClientFactory clientFactory = NettyClientFactory.INSTANCE;
    private int defaultTimeout = 3000;
    private int port = 4000;

    public void init() {
        try {
            ConfigCenter.INSTANCE.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> nodes = ZkUtil.instance.getChildrenKeys(PathEnum.GATEWAY_SERVER.getPath());
        if (nodes == null || nodes.isEmpty()) return;
        for (String name : nodes) {
            String json = ZkUtil.instance.get(PathEnum.GATEWAY_SERVER.getPathByName(name));
            ServerApp server = Jsons.fromJson(json, ServerApp.class);
            if (server == null) continue;
            createClient(server.getIp(), server.getPort());
        }
    }

    private void createClient(String ip, int port) {
        Client client = clientFactory.get(ip, port);
        if (client == null) {
            final Client cli = clientFactory.createGet(ip, port, new PushClientChannelHandler());
            ThreadPoolUtil.newThread(new Runnable() {
                @Override
                public void run() {
                    cli.init();
                    cli.start();
                }
            }, "push-client-" + ip).start();
        }
    }

    public Connection getConnection(String ip) {
        Client client = clientFactory.get(ip, port);
        if (client == null) return null;
        return ((PushClientChannelHandler) client.getHandler()).getConnection();
    }

    @Override
    public void send(String content, Collection<String> userIds, Callback callback) {
        for (String userId : userIds) {
            PushRequest
                    .build(this)
                    .setCallback(callback)
                    .setUserId(userId)
                    .setContent(content)
                    .setTimeout(defaultTimeout)
                    .send();
        }
    }
}
