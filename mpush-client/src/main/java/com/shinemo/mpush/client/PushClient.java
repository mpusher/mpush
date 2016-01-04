package com.shinemo.mpush.client;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.netty.client.NettyClientFactory;

import java.util.Collection;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushClient implements PushSender {

    public NettyClientFactory clientFactory;
    private String host = "127.0.0.1";
    private int port = 4000;
    private int defaultTimeout = 3000;

    public void init() throws Exception {
        this.clientFactory = NettyClientFactory.INSTANCE;
    }

    public Connection getConnection(String ip) {
        try {
            Client client = clientFactory.get(ip, port);
            if (client == null) {
                final Client client2 = clientFactory.createClient(ip,
                        port, new PushClientChannelHandler());
                client2.init();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client2.start();
                    }
                }).start();
                client = client2;
            }
            return ((PushClientChannelHandler) client.getHandler()).getConnection();
        } catch (Exception e) {

        }
        return null;
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
