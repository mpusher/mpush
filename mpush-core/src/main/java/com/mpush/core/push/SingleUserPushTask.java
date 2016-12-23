/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.core.push;

import com.mpush.api.connection.Connection;
import com.mpush.common.ErrorCode;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.common.message.PushMessage;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.router.RemoteRouter;
import com.mpush.core.ack.AckCallback;
import com.mpush.core.ack.AckTask;
import com.mpush.core.ack.AckTaskQueue;
import com.mpush.core.router.LocalRouter;
import com.mpush.core.router.RouterCenter;
import com.mpush.tools.log.Logs;

import java.util.concurrent.ScheduledExecutorService;

import static com.mpush.common.ErrorCode.OFFLINE;
import static com.mpush.common.ErrorCode.PUSH_CLIENT_FAILURE;
import static com.mpush.common.ErrorCode.ROUTER_CHANGE;
import static com.mpush.zk.node.ZKServerNode.GS_NODE;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class SingleUserPushTask implements PushTask {
    private final FlowControl flowControl;

    private final GatewayPushMessage message;

    public SingleUserPushTask(GatewayPushMessage message, FlowControl flowControl) {
        this.flowControl = flowControl;
        this.message = message;
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return message.getConnection().getChannel().eventLoop();
    }

    @Override
    public void run() {
        if (!checkLocal(message)) {
            checkRemote(message);
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

            Logs.PUSH.warn("[SingleUserPush] find local router but conn disconnected, message={}, conn={}", message, connection);

            //删除已经失效的本地路由
            RouterCenter.I.getLocalRouterManager().unRegister(userId, clientType);

            return false;
        }

        //3.检测TCP缓冲区是否已满且写队列超过最高阀值
        if (!connection.getChannel().isWritable()) {
            ErrorMessage.from(message).setErrorCode(PUSH_CLIENT_FAILURE).setData(userId + ',' + clientType).sendRaw();

            Logs.PUSH.error("[SingleUserPush] push message to client failure, tcp sender too busy, message={}, conn={}", message, connection);
            return true;
        }

        //4. 检测qps, 是否超过流控限制，如果超过则进队列延后发送
        if (flowControl.checkQps()) {
            //5.链接可用，直接下发消息到手机客户端
            PushMessage pushMessage = new PushMessage(message.content, connection);
            pushMessage.getPacket().flags = message.getPacket().flags;

            pushMessage.send(future -> {
                if (future.isSuccess()) {//推送成功

                    if (message.needAck()) {//需要客户端ACK, 添加等待客户端响应ACK的任务
                        addAckTask(message, pushMessage.getSessionId());
                    } else {
                        OkMessage.from(message).setData(userId + ',' + clientType).sendRaw();
                    }

                    Logs.PUSH.info("[SingleUserPush] push message to client success, message={}", message);

                } else {//推送失败

                    ErrorMessage.from(message).setErrorCode(PUSH_CLIENT_FAILURE).setData(userId + ',' + clientType).sendRaw();

                    Logs.PUSH.error("[SingleUserPush] push message to client failure, message={}, conn={}", message, connection);
                }
            });
        } else {//超过流控限制, 进队列延后发送
            PushCenter.I.delayTask(flowControl.getDelay(), this);
        }
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

            Logs.PUSH.info("[SingleUserPush] remote router not exists user offline, message={}", message);

            return;
        }

        //2.如果查出的远程机器是当前机器，说明路由已经失效，此时用户已下线，需要删除失效的缓存
        if (remoteRouter.getRouteValue().isThisPC(GS_NODE.getIp(), GS_NODE.getPort())) {

            ErrorMessage.from(message).setErrorCode(OFFLINE).setData(userId + ',' + clientType).sendRaw();

            //删除失效的远程缓存
            RouterCenter.I.getRemoteRouterManager().unRegister(userId, clientType);

            Logs.PUSH.info("[SingleUserPush] find remote router in this pc, but local router not exists, userId={}, clientType={}, router={}"
                    , userId, clientType, remoteRouter);

            return;
        }

        //3.否则说明用户已经跑到另外一台机器上了；路由信息发生更改，让PushClient重推
        ErrorMessage.from(message).setErrorCode(ROUTER_CHANGE).setData(userId + ',' + clientType).sendRaw();

        Logs.PUSH.info("[SingleUserPush] find router in another pc, userId={}, clientType={}, router={}", userId, clientType, remoteRouter);

    }

    /**
     * 添加ACK任务到队列, 等待客户端响应
     *
     * @param message   网关消息
     * @param messageId 下发到客户端待ack的消息的sessionId
     */
    private static void addAckTask(GatewayPushMessage message, int messageId) {
        // 因为要进队列，可以提前释放一些比较占用内存的字段，便于垃圾回收
        message.getPacket().body = null;//内存释放
        message.content = null;//内存释放

        AckTask task = AckTask
                .from(message, messageId)
                .setCallback(new GatewayPushAckCallback(message));
        AckTaskQueue.I.add(task, message.timeout);
    }

    private static class GatewayPushAckCallback implements AckCallback {
        private final GatewayPushMessage message;

        private GatewayPushAckCallback(GatewayPushMessage message) {
            this.message = message;
        }

        @Override
        public void onSuccess(AckTask task) {

            if (!message.getConnection().isConnected()) {
                Logs.PUSH.warn("receive client ack, gateway connection is closed, task={}", task);
                return;
            }

            OkMessage okMessage = OkMessage.from(message);
            okMessage.setData(message.userId + ',' + message.clientType);
            okMessage.sendRaw();
            Logs.PUSH.info("receive client ack and response gateway client success, task={}", task);
        }

        @Override
        public void onTimeout(AckTask task) {
            if (!message.getConnection().isConnected()) {
                Logs.PUSH.warn("push message timeout client not ack, gateway connection is closed, task={}", task);
                return;
            }
            ErrorMessage errorMessage = ErrorMessage.from(message);
            errorMessage.setData(message.userId + ',' + message.clientType);
            errorMessage.setErrorCode(ErrorCode.ACK_TIMEOUT);
            errorMessage.sendRaw();
            Logs.PUSH.warn("push message timeout client not ack, task={}", task);
        }
    }
}
