package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.router.Router;
import com.shinemo.mpush.common.ErrorCode;
import com.shinemo.mpush.common.handler.BaseMessageHandler;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.common.message.OkMessage;
import com.shinemo.mpush.common.message.PushMessage;
import com.shinemo.mpush.common.message.gateway.GatewayPushMessage;
import com.shinemo.mpush.common.router.RemoteRouter;
import com.shinemo.mpush.core.router.RouterCenter;
import com.shinemo.mpush.tools.MPushUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/30.
 */
public final class GatewayPushHandler extends BaseMessageHandler<GatewayPushMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(GatewayPushHandler.class);

    @Override
    public GatewayPushMessage decode(Packet packet, Connection connection) {
        return new GatewayPushMessage(packet, connection);
    }

    @Override
    public void handle(final GatewayPushMessage message) {
        Router<?> router = RouterCenter.INSTANCE.lookup(message.userId);
        if (router == null) {
            //1.路由信息不存在说明用户此时不在线
            ErrorMessage
                    .from(message)
                    .setErrorCode(ErrorCode.OFFLINE)
                    .send();
            LOGGER.warn("gateway push, router not exists user offline userId={}, content={}", message.userId, message.content);
        } else if (router.getRouteType() == Router.RouterType.LOCAL) {
            //2.如果是本地路由信息，说明用户链接在当前机器，直接把消息下发到客户端
            Connection connection = (Connection) router.getRouteValue();
            PushMessage pushMessage = new PushMessage(message.content, connection);
            pushMessage.send(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {//推送成功
                        OkMessage
                                .from(message)
                                .setData(message.userId)
                                .send();
                        LOGGER.info("gateway push message to client success userId={}, content={}", message.userId, message.content);
                    } else {//推送失败
                        ErrorMessage
                                .from(message)
                                .setErrorCode(ErrorCode.PUSH_CLIENT_FAILURE)
                                .send();
                        LOGGER.error("gateway push message to client failure userId={}, content={}", message.userId, message.content);
                    }
                }
            });
            LOGGER.info("gateway push, router in local userId={}, connection={}", message.userId, connection);
        } else {
            RemoteRouter r = (RemoteRouter) router;
            if (r.getRouteValue().getHost().equals(MPushUtil.getLocalIp())) {
                ErrorMessage
                        .from(message)
                        .setErrorCode(ErrorCode.OFFLINE)
                        .send();
                LOGGER.error("gateway push error remote is local, userId={}, router={}", message.userId, router);
                return;
            }
            //3.如果是远程路由，说明此时用户已经跑到另一台机器上了
            // 需要通过GatewayClient或ZK把消息推送到另外一台机器上
            ErrorMessage
                    .from(message)
                    .setErrorCode(ErrorCode.ROUTER_CHANGE)
                    .send();
            LOGGER.info("gateway push, router in remote userId={}, router={}", message.userId, router);
        }
    }
}
