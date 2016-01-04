package com.shinemo.mpush.client;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.common.message.gateway.GatewayPushMessage;
import com.shinemo.mpush.common.router.RemoteRouter;
import com.shinemo.mpush.common.router.RemoteRouterManager;
import com.shinemo.mpush.common.router.RouterCenter;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

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

    private Connection getConnection(String ip) {
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


    public void send(String content, final String userId, final PushRequest callback) {
        RemoteRouterManager remoteRouterManager = RouterCenter.INSTANCE.getRemoteRouterManager();
        RemoteRouter router = remoteRouterManager.lookup(userId);
        if (router == null) {
            callback.onOffline(userId);
            return;
        }
        ClientLocation location = router.getRouteValue();
        Connection connection = getConnection(location.getHost());
        if (connection == null || !connection.isConnected()) {
            callback.onFailure(userId);
            return;
        }
        GatewayPushMessage pushMessage = new GatewayPushMessage(userId, content, connection);
        PushRequestBus.INSTANCE.register(pushMessage.getSessionId(), callback);
        pushMessage.send(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                } else {
                    callback.onFailure(userId);
                }
            }
        });
    }
}
