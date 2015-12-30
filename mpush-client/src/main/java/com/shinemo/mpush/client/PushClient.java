package com.shinemo.mpush.client;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.common.MessageDispatcher;
import com.shinemo.mpush.common.handler.ErrorMessageHandler;
import com.shinemo.mpush.common.handler.OkMessageHandler;
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
    private PacketReceiver receiver;
    private String host = "127.0.0.1";
    private int port = 4000;
    private int defaultTimeout = 3000;

    public void init() throws Exception {
        this.clientFactory = NettyClientFactory.INSTANCE;
        MessageDispatcher receiver = new MessageDispatcher();
        receiver.register(Command.OK, new OkMessageHandler());
        receiver.register(Command.ERROR, new ErrorMessageHandler());
        this.receiver = receiver;
    }

    private Connection getConnection(String ip) {
        try {
            Client client = clientFactory.get(ip, port);
            if (client == null) {
                final Client client2 = clientFactory.createClient(ip,
                        port, new PushClientChannelHandler(receiver));
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
    public void send(String content, Collection<String> userIds, final Callback callback) {
        RemoteRouterManager remoteRouterManager = RouterCenter.INSTANCE.getRemoteRouterManager();
        for (final String userId : userIds) {
            final PushCallback cb = new PushCallback(callback, userId, defaultTimeout);
            RemoteRouter router = remoteRouterManager.lookup(userId);
            if (router == null) {
                cb.onOffline(userId);
                continue;
            }
            ClientLocation location = router.getRouteValue();
            Connection connection = getConnection(location.getHost());
            if (connection == null || !connection.isConnected()) {
                cb.onFailure(userId);
                continue;
            }

            GatewayPushMessage pushMessage = new GatewayPushMessage(userId
                    , "push content", connection);
            final int reqId = pushMessage.getSessionId();
            pushMessage.send(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        PushCallbackBus.INSTANCE.register(reqId, cb);
                    } else {
                        callback.onFailure(userId);
                    }
                }
            });
        }
    }
}
