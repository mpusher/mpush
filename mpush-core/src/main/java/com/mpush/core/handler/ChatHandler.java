package com.mpush.core.handler;

import static com.mpush.common.ErrorCode.OFFLINE;
import static com.mpush.common.ErrorCode.PUSH_CLIENT_FAILURE;
import static com.mpush.common.ErrorCode.ROUTER_CHANGE;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.ChatMessage;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.common.message.PushMessage;
import com.mpush.common.router.RemoteRouter;
import com.mpush.core.router.LocalRouter;
import com.mpush.core.router.RouterCenter;
import com.mpush.log.Logs;
import com.mpush.tools.MPushUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Push消息的处理Handler
 * @author shiruxu
 *
 */
public class ChatHandler extends BaseMessageHandler<ChatMessage> {

	@Override
	public ChatMessage decode(Packet packet, Connection connection) {
		return new ChatMessage(packet, connection);
	}

	@Override
	public void handle(ChatMessage message) {
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
    private boolean checkLocal(final ChatMessage message) {
        LocalRouter router = RouterCenter.INSTANCE.getLocalRouterManager().lookup(message.destUserId);

        //1.如果本机不存在，再查下远程，看用户是否登陆到其他机器
        if (router == null) return false;

        Connection connection = router.getRouteValue();

        //2.如果链接失效，先删除本地失效的路由，再查下远程路由，看用户是否登陆到其他机器
        if (!connection.isConnected()) {
            Logs.PUSH.info("chat push, router in local but disconnect, userId={}, connection={}", message.destUserId, connection);

            //删除已经失效的本地路由
            RouterCenter.INSTANCE.getLocalRouterManager().unRegister(message.destUserId);

            return false;
        }

        //3.链接可用，直接下发消息到手机客户端
        PushMessage pushMessage = new PushMessage(message.content, connection);

        pushMessage.send(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    //推送成功
                    OkMessage.from(message).setData(message.destUserId).send();

                    Logs.PUSH.info("chat push message to client success userId={}, content={}", message.destUserId, message.content);

                } else {
                    //推送失败
                    ErrorMessage.from(message).setErrorCode(PUSH_CLIENT_FAILURE).send();

                    Logs.PUSH.info("gateway push message to client failure userId={}, content={}", message.destUserId, message.content);
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
    private void checkRemote(ChatMessage message) {
        RemoteRouter router = RouterCenter.INSTANCE.getRemoteRouterManager().lookup(message.destUserId);

        // 1.如果远程路由信息也不存在, 说明用户此时不在线，
        if (router == null) {

            ErrorMessage.from(message).setErrorCode(OFFLINE).send();

            Logs.PUSH.info("chat push, router not exists user offline userId={}, content={}", message.destUserId, message.content);

            return;
        }

        //2.如果查出的远程机器是当前机器，说明路由已经失效，此时用户已下线，需要删除失效的缓存
        if (MPushUtil.getLocalIp().equals(router.getRouteValue().getHost())) {

            ErrorMessage.from(message).setErrorCode(OFFLINE).send();

            //删除失效的远程缓存
            RouterCenter.INSTANCE.getRemoteRouterManager().unRegister(message.destUserId);

            Logs.PUSH.info("chat push error remote is local, userId={}, router={}", message.destUserId, router);

            return;
        }

        //3.否则说明用户已经跑到另外一台机器上了；路由信息发生更改，让PushClient重推
        ErrorMessage.from(message).setErrorCode(ROUTER_CHANGE).send();

        Logs.PUSH.info("chat push, router in remote userId={}, router={}", message.destUserId, router);

    }
}
