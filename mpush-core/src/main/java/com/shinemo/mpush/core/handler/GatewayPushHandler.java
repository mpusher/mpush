package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.handler.BaseMessageHandler;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.common.message.OkMessage;
import com.shinemo.mpush.common.message.PushMessage;
import com.shinemo.mpush.common.message.gateway.GatewayPushMessage;
import com.shinemo.mpush.common.router.RemoteRouter;
import com.shinemo.mpush.core.router.LocalRouter;
import com.shinemo.mpush.core.router.RouterCenter;
import com.shinemo.mpush.tools.MPushUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * 处理PushClient发送过来的Push推送请求
     * <p>
     * 查推送策略，先查本地路由，本地不存在，查远程，（注意：有可能远程也是本机）
     * <p>
     * 正常情况本地路由应该存在，如果不存在或链接失效，有以下几种情况：
     * <p>
     * 1.客户端重连，并且链接到了其他机器
     * 2.客户端下线，本地路由失效，远程路由还未清除
     * 3.PushClient使用了本地缓存，但缓存数据已经和实际情况不一致了
     * <p>
     * 对于三种情况的处理方式是, 再检查下远程路由：
     * 1.如果发现远程路由是本机，直接删除，因为此时的路由已失效
     * 2.如果用户真在另一台机器，让PushClient清理下本地缓存后，重新推送
     * <p>
     *
     * @param message
     */
    @Override
    public void handle(GatewayPushMessage message) {
        if (!checkLocal(message)) {
            checkRemote(message);
        }
    }

    /**
     * 检查本地路由，如果存在并且链接可用直接推送
     * 否则要检查下远程路由
     *
     * @param message
     * @return
     */
    private boolean checkLocal(final GatewayPushMessage message) {
        LocalRouter router = RouterCenter.INSTANCE.getLocalRouterManager().lookup(message.userId);

        //1.如果本机不存在，再查下远程，看用户是否登陆到其他机器
        if (router == null) return false;

        Connection connection = router.getRouteValue();

        //2.如果链接失效，先删除本地失效的路由，再查下远程路由，看用户是否登陆到其他机器
        if (!connection.isConnected()) {

            LOGGER.info("gateway push, router in local but disconnect, userId={}, connection={}", message.userId, connection);

            //删除已经失效的本地路由
            RouterCenter.INSTANCE.getLocalRouterManager().unRegister(message.userId);

            return false;
        }

        //3.链接可用，直接下发消息到手机客户端
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
        return true;
    }

    /**
     * 检测远程路由，
     * 如果不存在直接返回用户已经下线
     * 如果是本机直接删除路由信息
     * 如果是其他机器让PushClient重推
     *
     * @param message
     */
    private void checkRemote(GatewayPushMessage message) {
        RemoteRouter router = RouterCenter.INSTANCE.getRemoteRouterManager().lookup(message.userId);

        // 1.如果远程路由信息也不存在, 说明用户此时不在线，
        if (router == null) {

            ErrorMessage.from(message).setErrorCode(OFFLINE).send();

            LOGGER.warn("gateway push, router not exists user offline userId={}, content={}", message.userId, message.content);

            return;
        }

        //2.如果查出的远程机器是当前机器，说明路由已经失效，此时用户已下线，需要删除失效的缓存
        if (MPushUtil.getLocalIp().equals(router.getRouteValue().getHost())) {

            ErrorMessage.from(message).setErrorCode(OFFLINE).send();

            //删除失效的远程缓存
            RouterCenter.INSTANCE.getRemoteRouterManager().unRegister(message.userId);

            LOGGER.error("gateway push error remote is local, userId={}, router={}", message.userId, router);

            return;
        }

        //3.否则说明用户已经跑到另外一台机器上了；路由信息发生更改，让PushClient重推
        ErrorMessage.from(message).setErrorCode(ROUTER_CHANGE).send();

        LOGGER.info("gateway push, router in remote userId={}, router={}", message.userId, router);
    }
}
