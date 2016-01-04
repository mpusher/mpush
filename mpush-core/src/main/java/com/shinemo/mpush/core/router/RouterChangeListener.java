package com.shinemo.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.event.RouterChangeEvent;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.api.router.Router;
import com.shinemo.mpush.common.EventBus;
import com.shinemo.mpush.common.message.KickUserMessage;
import com.shinemo.mpush.common.router.RemoteRouter;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2016/1/4.
 */
public class RouterChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterChangeListener.class);

    public RouterChangeListener() {
        EventBus.INSTANCE.register(this);
        // TODO: 2016/1/4 register this to redis server
    }

    @Subscribe
    void onRouteChangeEvent(RouterChangeEvent event) {
        String userId = event.userId;
        Router<?> r = event.router;
        if (r.getRouteType() == Router.RouterType.LOCAL) {
            kickLocal(userId, (LocalRouter) r);
        } else {
            kickRemote(userId, (RemoteRouter) r);
        }
    }

    public void kickLocal(final String userId, LocalRouter router) {
        Connection connection = router.getRouteValue();
        SessionContext context = connection.getSessionContext();
        KickUserMessage message = new KickUserMessage(connection);
        message.deviceId = context.deviceId;
        message.userId = userId;
        message.send(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOGGER.info("kick local connection success, userId={}", userId);
                } else {
                    LOGGER.error("kick local connection failure, userId={}", userId);
                }
            }
        });
    }


    public void kickRemote(String userId, RemoteRouter router) {
        ClientLocation location = router.getRouteValue();
        KickRemoteMsg msg = new KickRemoteMsg();
        msg.deviceId = location.getDeviceId();
        msg.srcServer = location.getHost();
        msg.userId = userId;
        // TODO: 2016/1/4 publish kick remote user msg to redis
    }

    // TODO: 2016/1/4  receive msg from redis
    public void onReceiveKickRemoteMsg(KickRemoteMsg msg) {
        String userId = msg.userId;
        LocalRouter router = RouterCenter.INSTANCE.getLocalRouterManager().lookup(userId);
        if (router != null) {
            LOGGER.info("receive kick remote msg, msg={}", msg);
            kickLocal(userId, router);
        } else {
            LOGGER.warn("no local router find, kick failure, msg={}", msg);
        }
    }
}
