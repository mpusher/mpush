package com.shinemo.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.event.RouterChangeEvent;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.api.router.Router;
import com.shinemo.mpush.common.AbstractEventContainer;
import com.shinemo.mpush.common.message.KickUserMessage;
import com.shinemo.mpush.common.router.RemoteRouter;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.redis.listener.ListenerDispatcher;
import com.shinemo.mpush.tools.redis.listener.MessageListener;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2016/1/4.
 */
public final class RouterChangeListener extends AbstractEventContainer implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterChangeListener.class);
    public static final String KICK_CHANNEL_ = "/mpush/kick/";
    private final String kick_channel = KICK_CHANNEL_ + MPushUtil.getLocalIp();

    public RouterChangeListener() {
    	ListenerDispatcher.INSTANCE.subscribe(getKickChannel(), this);
    }

    public String getKickChannel() {
        return kick_channel;
    }
    
    public String getKickChannel(String remoteIp){
    	return KICK_CHANNEL_ + remoteIp;
    }

    @Subscribe
    void onRouteChangeEvent(RouterChangeEvent event) {
        String userId = event.userId;
        Router<?> r = event.router;
        if (r.getRouteType().equals(Router.RouterType.LOCAL)) {
            kickLocal(userId, (LocalRouter) r);
        } else {
            kickRemote(userId, (RemoteRouter) r);
        }

        // TODO: 2016/1/10 publish remoter change event to redis
    }

    /**
     * 发送踢人消息到客户端
     *
     * @param userId
     * @param router
     */
    public void kickLocal(final String userId, final LocalRouter router) {
        Connection connection = router.getRouteValue();
        SessionContext context = connection.getSessionContext();
        KickUserMessage message = new KickUserMessage(connection);
        message.deviceId = context.deviceId;
        message.userId = userId;
        message.send(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().close();
                if (future.isSuccess()) {
                    LOGGER.info("kick local connection success, userId={}, router={}", userId, router);
                } else {
                    LOGGER.error("kick local connection failure, userId={}, router={}", userId, router);
                }
            }
        });
    }

    /**
     * 广播踢人消息到消息中心（redis）.
     * <p>
     * 有可能目标机器是当前机器，所以要做一次过滤
     * 如果client连续2次链接到同一台机器上就有会出现这中情况
     *
     * @param userId
     * @param router
     */
    public void kickRemote(String userId, RemoteRouter router) {
        ClientLocation location = router.getRouteValue();
        //1.如果目标机器是当前机器，就不要再发送广播了，直接忽略
        if (location.getHost().equals(MPushUtil.getLocalIp())) {
            LOGGER.error("kick remote user but router in local, userId={}", userId);
            return;
        }

        //2.发送广播
        //TODO 远程机器可能不存在，需要确认下redis 那个通道如果机器不存在的话，是否会存在消息积压的问题。
        KickRemoteMsg msg = new KickRemoteMsg();
        msg.deviceId = location.getDeviceId();
        msg.targetServer = location.getHost();
        msg.userId = userId;
        RedisManage.publish(getKickChannel(msg.targetServer), msg);
    }

    /**
     * 处理远程机器发送的踢人广播.
     * <p>
     * 一台机器发送广播所有的机器都能收到，
     * 包括发送广播的机器，所有要做一次过滤
     *
     * @param msg
     */
    public void onReceiveKickRemoteMsg(KickRemoteMsg msg) {
        //1.如果当前机器不是目标机器，直接忽略
        if (!msg.targetServer.equals(MPushUtil.getLocalIp())) {
            LOGGER.error("receive kick remote msg, target server error, localIp={}, msg={}", MPushUtil.getLocalIp(), msg);
            return;
        }

        //2.查询本地路由，找到要被踢下线的链接，并删除该本地路由
        String userId = msg.userId;
        LocalRouterManager routerManager = RouterCenter.INSTANCE.getLocalRouterManager();
        LocalRouter router = routerManager.lookup(userId);
        if (router != null) {
            LOGGER.info("receive kick remote msg, msg={}", msg);
            //2.1删除本地路由信息
            routerManager.unRegister(userId);
            //2.2发送踢人消息到客户端
            kickLocal(userId, router);
        } else {
            LOGGER.warn("no local router find, kick failure, msg={}", msg);
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        if (getKickChannel().equals(channel)) {
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
