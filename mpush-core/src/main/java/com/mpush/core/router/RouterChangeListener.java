/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.RouterChangeEvent;
import com.mpush.api.router.ClientLocation;
import com.mpush.api.router.Router;
import com.mpush.cache.redis.listener.ListenerDispatcher;
import com.mpush.cache.redis.listener.MessageListener;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.common.message.KickUserMessage;
import com.mpush.common.message.gateway.GatewayKickUserMessage;
import com.mpush.common.net.KickRemoteMsg;
import com.mpush.common.router.RemoteRouter;
import com.mpush.core.server.GatewayUDPConnector;
import com.mpush.tools.Jsons;
import com.mpush.tools.Utils;
import com.mpush.tools.config.CC;
import com.mpush.tools.event.EventConsumer;
import com.mpush.tools.log.Logs;

import java.net.InetSocketAddress;

import static com.mpush.zk.node.ZKServerNode.GS_NODE;

/**
 * Created by ohun on 2016/1/4.
 *
 * @author ohun@live.cn
 */
public final class RouterChangeListener extends EventConsumer implements MessageListener {
    public static final String KICK_CHANNEL_ = "/mpush/kick/";
    private final String kick_channel = KICK_CHANNEL_ + GS_NODE.getHostAndPort();

    public RouterChangeListener() {
        ListenerDispatcher.I.subscribe(getKickChannel(), this);
    }

    public String getKickChannel() {
        return kick_channel;
    }

    public String getKickChannel(String hostAndPort) {
        return KICK_CHANNEL_ + hostAndPort;
    }

    @Subscribe
    void on(RouterChangeEvent event) {
        String userId = event.userId;
        Router<?> r = event.router;
        if (r.getRouteType().equals(Router.RouterType.LOCAL)) {
            kickLocal(userId, (LocalRouter) r);
        } else {
            kickRemote(userId, (RemoteRouter) r);
        }
    }

    /**
     * 发送踢人消息到客户端
     *
     * @param userId
     * @param router
     */
    private void kickLocal(final String userId, final LocalRouter router) {
        Connection connection = router.getRouteValue();
        SessionContext context = connection.getSessionContext();
        KickUserMessage message = new KickUserMessage(connection);
        message.deviceId = context.deviceId;
        message.userId = userId;
        message.send(future -> {
            if (future.isSuccess()) {
                Logs.CONN.info("kick local connection success, userId={}, router={}, conn={}", userId, router, connection);
            } else {
                Logs.CONN.warn("kick local connection failure, userId={}, router={}, conn={}", userId, router, connection);
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
     * @param remoteRouter
     */
    private void kickRemote(String userId, RemoteRouter remoteRouter) {
        ClientLocation location = remoteRouter.getRouteValue();
        //1.如果目标机器是当前机器，就不要再发送广播了，直接忽略
        if (location.isThisPC(GS_NODE.getIp(), GS_NODE.getPort())) {
            Logs.CONN.debug("kick remote router in local pc, ignore remote broadcast, userId={}", userId);
            return;
        }

        if (CC.mp.net.udpGateway()) {
            Connection connection = GatewayUDPConnector.I().getConnection();
            GatewayKickUserMessage.build(connection)
                    .setUserId(userId)
                    .setClientType(location.getClientType())
                    .setConnId(location.getConnId())
                    .setDeviceId(location.getDeviceId())
                    .setTargetServer(location.getHost())
                    .setTargetPort(location.getPort())
                    .setRecipient(new InetSocketAddress(location.getHost(), location.getPort()))
                    .sendRaw();
        } else {
            //2.发送广播
            //TODO 远程机器可能不存在，需要确认下redis 那个通道如果机器不存在的话，是否会存在消息积压的问题。
            RedisKickRemoteMessage message = new RedisKickRemoteMessage()
                    .setUserId(userId)
                    .setClientType(location.getClientType())
                    .setConnId(location.getConnId())
                    .setDeviceId(location.getDeviceId())
                    .setTargetServer(location.getHost())
                    .setTargetPort(location.getPort());
            RedisManager.I.publish(getKickChannel(location.getHostAndPort()), message);
        }
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
        if (!msg.isTargetPC()) {
            Logs.CONN.error("receive kick remote msg, target server error, localIp={}, msg={}", Utils.getLocalIp(), msg);
            return;
        }

        //2.查询本地路由，找到要被踢下线的链接，并删除该本地路由
        String userId = msg.getUserId();
        int clientType = msg.getClientType();
        LocalRouterManager localRouterManager = RouterCenter.I.getLocalRouterManager();
        LocalRouter localRouter = localRouterManager.lookup(userId, clientType);
        if (localRouter != null) {
            Logs.CONN.info("receive kick remote msg, msg={}", msg);
            if (localRouter.getRouteValue().getId().equals(msg.getConnId())) {//二次校验，防止误杀
                //2.1删除本地路由信息
                localRouterManager.unRegister(userId, clientType);
                //2.2发送踢人消息到客户端
                kickLocal(userId, localRouter);
            } else {
                Logs.CONN.warn("kick router failure target connId not match, localRouter={}, msg={}", localRouter, msg);
            }
        } else {
            Logs.CONN.warn("kick router failure can't find local router, msg={}", msg);
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        if (getKickChannel().equals(channel)) {
            KickRemoteMsg msg = Jsons.fromJson(message, KickRemoteMsg.class);
            if (msg != null) {
                onReceiveKickRemoteMsg(msg);
            } else {
                Logs.CONN.warn("receive an error kick message={}", message);
            }
        } else {
            Logs.CONN.warn("receive an error redis channel={}", channel);
        }
    }
}
