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

package com.mpush.core.handler;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.ErrorCode;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.common.message.PushMessage;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.router.RemoteRouter;
import com.mpush.common.user.UserManager;
import com.mpush.core.ack.AckCallback;
import com.mpush.core.ack.AckContext;
import com.mpush.core.ack.AckMessageQueue;
import com.mpush.core.router.LocalRouter;
import com.mpush.core.router.LocalRouterManager;
import com.mpush.core.router.RouterCenter;
import com.mpush.tools.Jsons;
import com.mpush.tools.Utils;
import com.mpush.tools.common.Pair;
import com.mpush.tools.log.Logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mpush.api.protocol.Command.ERROR;
import static com.mpush.api.protocol.Command.OK;
import static com.mpush.common.ErrorCode.*;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public final class GatewayPushHandler extends BaseMessageHandler<GatewayPushMessage> {

    @Override
    public GatewayPushMessage decode(Packet packet, Connection connection) {
        return new GatewayPushMessage(packet, connection);
    }

    /**
     * 处理PushClient发送过来的Push推送请求
     * <p>
     * 查寻路由策略，先查本地路由，本地不存在，查远程，（注意：有可能远程查到也是本机IP）
     * <p>
     * 正常情况本地路由应该存在，如果不存在或链接失效，有以下几种情况：
     * <p>
     * 1.客户端重连，并且链接到了其他机器
     * 2.客户端下线，本地路由失效，远程路由还未清除
     * 3.PushClient使用了本地缓存，但缓存数据已经和实际情况不一致了
     * <p>
     * 对于三种情况的处理方式是, 再重新查寻下远程路由：
     * 1.如果发现远程路由是本机，直接删除，因为此时的路由已失效 (解决场景2)
     * 2.如果用户真在另一台机器，让PushClient清理下本地缓存后，重新推送 (解决场景1,3)
     * <p>
     *
     * @param message message
     */
    @Override
    public void handle(GatewayPushMessage message) {
        if (message.isBroadcast()) {
            sendBroadcast(message);
        } else {
            if (!checkLocal(message)) {
                checkRemote(message);
            }
        }
    }

    /**
     * 广播所有在线用户
     *
     * @param message message
     */
    private void sendBroadcast(GatewayPushMessage message) {
        Set<String> sendUserIds = new CopyOnWriteArraySet<>();
        Set<String> tagList = message.tags;
        LocalRouterManager routerManager = RouterCenter.I.getLocalRouterManager();
        AtomicInteger tasks = new AtomicInteger();//总任务数, 0表示任务全部结束
        long begin = System.currentTimeMillis();
        //TODO 考虑使用线程池批量推送
        for (int start = 0, limit = 1000; ; start += limit) {
            List<String> userIds = UserManager.I.getOnlineUserList(start, limit);
            tasks.addAndGet(userIds.size());//增加任务数
            userIds.forEach(userId -> {
                for (LocalRouter router : routerManager.lookupAll(userId)) {
                    Connection connection = router.getRouteValue();
                    int clientType = router.getClientType();

                    //2.按标签过滤,目前只有include,后续会增加exclude
                    String tags = connection.getSessionContext().tags;
                    if (tagList != null && tags != null) {
                        if (tagList.stream().noneMatch(tags::contains)) break;
                    }

                    if (connection.isConnected()) {

                        if (connection.getChannel().isWritable()) {//检测TCP缓冲区是否已满且写队列超过最高阀值
                            //3.链接可用，直接下发消息到手机客户端
                            PushMessage pushMessage = new PushMessage(message.content, connection);
                            pushMessage.getPacket().flags = message.getPacket().flags;

                            pushMessage.send(future -> {

                                if (!sendUserIds.contains(userId)) {
                                    tasks.decrementAndGet();//完成一个任务
                                }

                                if (future.isSuccess()) {//推送成功
                                    sendUserIds.add(userId);
                                    Logs.PUSH.info("<<< gateway broadcast client success, userId={}, message={}", userId, message);

                                } else {//推送失败
                                    Logs.PUSH.info("gateway broadcast client failure, userId={}, message={}", userId, message);
                                }

                                if (tasks.get() == 0) {//任务全部结束
                                    Logs.PUSH.info("gateway broadcast finished, cost={}, message={}", (System.currentTimeMillis() - begin), message);
                                    OkMessage.from(message).setData(Jsons.toJson(sendUserIds)).sendRaw();
                                }
                            });
                        } else {
                            tasks.decrementAndGet();//完成一个任务
                            Logs.PUSH.info("gateway broadcast client failure, send too busy, userId={}, message={}", userId, message);
                        }
                    } else { //2.如果链接失效，先删除本地失效的路由，再查下远程路由，看用户是否登陆到其他机器
                        Logs.PUSH.info("gateway broadcast, router in local but disconnect, message={}", message);

                        tasks.decrementAndGet();//完成一个任务

                        //删除已经失效的本地路由
                        RouterCenter.I.getLocalRouterManager().unRegister(userId, clientType);

                        if (tasks.get() == 0) {//任务全部结束
                            Logs.PUSH.info("gateway broadcast finished, cost={}, message={}", (System.currentTimeMillis() - begin), message);
                            OkMessage.from(message).setData(Jsons.toJson(sendUserIds)).sendRaw();
                        }
                    }
                }
            });

            if (userIds.size() != limit) break;//查询完毕
        }
    }

    /**
     * 检查本地路由，如果存在并且链接可用直接推送
     * 否则要检查下远程路由
     *
     * @param message message
     * @return true/false true:success
     */

    private boolean checkLocal(final GatewayPushMessage message) {
        String userId = message.userId;
        int clientType = message.clientType;
        LocalRouter localRouter = RouterCenter.I.getLocalRouterManager().lookup(userId, clientType);

        //1.如果本机不存在，再查下远程，看用户是否登陆到其他机器
        if (localRouter == null) return false;

        Connection connection = localRouter.getRouteValue();

        //2.如果链接失效，先删除本地失效的路由，再查下远程路由，看用户是否登陆到其他机器
        if (!connection.isConnected()) {

            Logs.PUSH.info("gateway push, router in local but disconnect, message={}", message, connection);

            //删除已经失效的本地路由
            RouterCenter.I.getLocalRouterManager().unRegister(userId, clientType);

            return false;
        }

        //3.检测TCP缓冲区是否已满且写队列超过最高阀值
        if (!connection.getChannel().isWritable()) {
            ErrorMessage.from(message).setErrorCode(PUSH_CLIENT_FAILURE).setData(userId + ',' + clientType).sendRaw();

            Logs.PUSH.info("gateway push message to client failure, send too busy, message={}", message);
            return true;
        }

        //4.链接可用，直接下发消息到手机客户端
        PushMessage pushMessage = new PushMessage(message.content, connection);
        pushMessage.getPacket().flags = message.getPacket().flags;

        pushMessage.send(future -> {
            if (future.isSuccess()) {//推送成功

                if (message.needAck()) {//需要客户端ACK, 消息进队列等待客户端响应ACK
                    AckMessageQueue.I.put(pushMessage.getSessionId(), buildAckContext(message), message.timeout);
                } else {
                    OkMessage.from(message).setData(userId + ',' + clientType).sendRaw();
                }

                Logs.PUSH.info("<<< gateway push message to client success, message={}", message);

            } else {//推送失败

                ErrorMessage.from(message).setErrorCode(PUSH_CLIENT_FAILURE).setData(userId + ',' + clientType).sendRaw();

                Logs.PUSH.info("gateway push message to client failure, message={}", message);
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
     * @param message message
     */
    private void checkRemote(GatewayPushMessage message) {
        String userId = message.userId;
        int clientType = message.clientType;
        RemoteRouter remoteRouter = RouterCenter.I.getRemoteRouterManager().lookup(userId, clientType);

        // 1.如果远程路由信息也不存在, 说明用户此时不在线，
        if (remoteRouter == null || remoteRouter.isOffline()) {

            ErrorMessage.from(message).setErrorCode(OFFLINE).setData(userId + ',' + clientType).sendRaw();

            Logs.PUSH.info("gateway push, router not exists user offline, message={}", message);

            return;
        }

        //2.如果查出的远程机器是当前机器，说明路由已经失效，此时用户已下线，需要删除失效的缓存
        if (Utils.getLocalIp().equals(remoteRouter.getRouteValue().getHost())) {

            ErrorMessage.from(message).setErrorCode(OFFLINE).setData(userId + ',' + clientType).sendRaw();

            //删除失效的远程缓存
            RouterCenter.I.getRemoteRouterManager().unRegister(userId, clientType);

            Logs.PUSH.info("gateway push error remote is local, userId={}, clientType={}, router={}", userId, clientType, remoteRouter);

            return;
        }

        //3.否则说明用户已经跑到另外一台机器上了；路由信息发生更改，让PushClient重推
        ErrorMessage.from(message).setErrorCode(ROUTER_CHANGE).setData(userId + ',' + clientType).sendRaw();

        Logs.PUSH.info("gateway push, router in remote userId={}, clientType={}, router={}", userId, clientType, remoteRouter);

    }


    private AckContext buildAckContext(GatewayPushMessage message) {
        Connection gatewayConnection = message.getConnection();
        String userId = message.userId;
        int clientType = message.clientType;

        return AckContext.from(message)
                .setCallback(new AckCallback() {
                    @Override
                    public void onSuccess(AckContext ctx) {
                        if (!gatewayConnection.isConnected()) {
                            Logs.PUSH.info(">>> receive client ack, gateway connection is closed, context={}", ctx);
                            return;
                        }

                        OkMessage okMessage = new OkMessage(ctx.cmd, new Packet(OK, ctx.gatewayMessageId), gatewayConnection);
                        okMessage.setData(userId + ',' + clientType);
                        okMessage.sendRaw();
                        Logs.PUSH.info(">>> receive client ack and response gateway client success, context={}", ctx);
                    }

                    @Override
                    public void onTimeout(AckContext ctx) {
                        if (!gatewayConnection.isConnected()) {
                            Logs.PUSH.info("push message timeout client not ack, gateway connection is closed, context={}", ctx);
                            return;
                        }
                        ErrorMessage errorMessage = new ErrorMessage(ctx.cmd, new Packet(ERROR, ctx.gatewayMessageId), gatewayConnection);
                        errorMessage.setData(userId + ',' + clientType);
                        errorMessage.setErrorCode(ErrorCode.ACK_TIMEOUT);
                        errorMessage.sendRaw();
                        Logs.PUSH.info("push message timeout client not ack, context={}", ctx);
                    }
                });
    }
}
