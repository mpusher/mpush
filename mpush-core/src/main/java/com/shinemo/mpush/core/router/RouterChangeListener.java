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
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.redis.listener.MessageListener;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import com.shinemo.mpush.tools.redis.pubsub.Subscriber;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2016/1/4.
 */
public class RouterChangeListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterChangeListener.class);
    public static final String KICK_CHANNEL = "__kick__";

    public RouterChangeListener() {
        EventBus.INSTANCE.register(this);
        RedisManage.subscribe(this, KICK_CHANNEL);
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
        if (location.getHost().equals(MPushUtil.getLocalIp())) {
            LOGGER.error("kick remote user but router in local, userId={}", userId);
            return;
        }
        KickRemoteMsg msg = new KickRemoteMsg();
        msg.deviceId = location.getDeviceId();
        msg.srcServer = location.getHost();
        msg.userId = userId;
        RedisManage.publish(KICK_CHANNEL, msg);
    }

    public void onReceiveKickRemoteMsg(KickRemoteMsg msg) {
        String userId = msg.userId;
        LocalRouterManager routerManager = RouterCenter.INSTANCE.getLocalRouterManager();
        LocalRouter router = routerManager.lookup(userId);
        if (router != null) {
            LOGGER.info("receive kick remote msg, msg={}", msg);
            routerManager.unRegister(userId);
            kickLocal(userId, router);
        } else {
            LOGGER.warn("no local router find, kick failure, msg={}", msg);
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        if (KICK_CHANNEL.equals(channel)) {
            KickRemoteMsg msg = Jsons.fromJson(message, KickRemoteMsg.class);
            if (msg != null) {
                onReceiveKickRemoteMsg(msg);
            } else {
                LOGGER.warn("receive an error kick message={}", message);
            }
        } else {
            LOGGER.warn("receive an error redis channel={}", channel);
        }
    }
}
