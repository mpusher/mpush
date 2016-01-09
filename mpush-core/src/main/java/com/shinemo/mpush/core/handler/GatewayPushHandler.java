package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.api.router.Router;
import com.shinemo.mpush.common.handler.BaseMessageHandler;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.common.message.OkMessage;
import com.shinemo.mpush.common.message.PushMessage;
import com.shinemo.mpush.common.message.gateway.GatewayPushMessage;
import com.shinemo.mpush.core.router.RouterCenter;
import com.shinemo.mpush.tools.MPushUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.shinemo.mpush.api.router.Router.RouterType.LOCAL;
import static com.shinemo.mpush.common.ErrorCode.*;

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
        //查询路由信息，先查本地，本地不存在，查远程，有可能远程也是本机(在本地被删除的情况下)
        Router<?> router = RouterCenter.INSTANCE.lookup(message.userId);

        if (router == null) {

            //1.路由信息不存在说明用户此时不在线
            ErrorMessage.from(message).setErrorCode(OFFLINE).send();

            LOGGER.warn("gateway push, router not exists user offline userId={}, content={}", message.userId, message.content);
        } else if (router.getRouteType() == LOCAL) {

            //2.如果是本地路由信息，说明用户链接在当前机器，如果链接可用，直接把消息下发到客户端
            Connection connection = (Connection) router.getRouteValue();

            if (!connection.isConnected()) {

                //2.1如果链接失效，先删除本地失效的路由，再查下远程路由，看用户是否登陆到其他机器
                LOGGER.info("gateway push, router in local but disconnect, userId={}, connection={}", message.userId, connection);

                //2.2删除已经失效的本地路由，防止递归死循环
                RouterCenter.INSTANCE.getLocalRouterManager().unRegister(message.userId);

                //2.3递归再试一次，看用户是否登陆在远程其他机器
                this.handle(message);

                return;
            }

            //2.4下发消息到手机客户端
            PushMessage pushMessage = new PushMessage(message.content, connection);

            pushMessage.send(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        //推送成功
                        OkMessage.from(message).setData(message.userId).send();

                        LOGGER.info("gateway push message to client success userId={}, content={}", message.userId, message.content);
                    } else {
                        //推送失败
                        ErrorMessage.from(message).setErrorCode(PUSH_CLIENT_FAILURE).send();

                        LOGGER.error("gateway push message to client failure userId={}, content={}", message.userId, message.content);
                    }
                }
            });

            LOGGER.info("gateway push, router in local userId={}, connection={}", message.userId, connection);
        } else {

            //3.如果是远程路由，说明此时用户已经跑到另一台机器上了
            ClientLocation location = (ClientLocation) router.getRouteValue();

            if (MPushUtil.getLocalIp().equals(location.getHost())) {
                //3.1如果查出的远程机器是当前机器，说明本机路由已经失效，此时说明用户已经不在线
                ErrorMessage.from(message).setErrorCode(OFFLINE).send();

                //3.2出现这种情况一般是pushClient使用了本地缓存导致的数据不一致，此时应清理下缓存
                RouterCenter.INSTANCE.getRemoteRouterManager().unRegister(message.userId);

                LOGGER.error("gateway push error remote is local, userId={}, router={}", message.userId, router);

                return;
            }

            //3.2返回给推送服务，路由信息发生更改
            ErrorMessage.from(message).setErrorCode(ROUTER_CHANGE).send();

            LOGGER.info("gateway push, router in remote userId={}, router={}", message.userId, router);
        }
    }
}
