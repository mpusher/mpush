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

import com.mpush.api.Message;
import com.mpush.api.connection.Connection;
import com.mpush.api.spi.push.IPushMessage;
import com.mpush.common.message.PushMessage;
import com.mpush.common.router.RemoteRouter;
import com.mpush.core.ack.AckTask;
import com.mpush.core.ack.AckTaskQueue;
import com.mpush.common.qps.FlowControl;
import com.mpush.core.router.LocalRouter;
import com.mpush.core.router.RouterCenter;
import com.mpush.tools.log.Logs;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.ScheduledExecutorService;

import static com.mpush.zk.node.ZKServerNode.GS_NODE;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class SingleUserPushTask implements PushTask {
    private final FlowControl flowControl;

    private final IPushMessage message;

    public SingleUserPushTask(IPushMessage message, FlowControl flowControl) {
        this.flowControl = flowControl;
        this.message = message;
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return ((Message) message).getConnection().getChannel().eventLoop();
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
     */
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
    private boolean checkLocal(IPushMessage message) {
        String userId = message.getUserId();
        int clientType = message.getClientType();
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
            PushCenter.I.getPushListener().onFailure(message);

            Logs.PUSH.error("[SingleUserPush] push message to client failure, tcp sender too busy, message={}, conn={}", message, connection);
            return true;
        }

        //4. 检测qps, 是否超过流控限制，如果超过则进队列延后发送
        if (flowControl.checkQps()) {
            //5.链接可用，直接下发消息到手机客户端
            PushMessage pushMessage = new PushMessage(message.getContent(), connection);
            pushMessage.getPacket().addFlag(message.getFlags());
            pushMessage.send(new PushFutureListener(message, pushMessage.getSessionId()));

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
    private void checkRemote(IPushMessage message) {
        String userId = message.getUserId();
        int clientType = message.getClientType();
        RemoteRouter remoteRouter = RouterCenter.I.getRemoteRouterManager().lookup(userId, clientType);

        // 1.如果远程路由信息也不存在, 说明用户此时不在线，
        if (remoteRouter == null || remoteRouter.isOffline()) {

            PushCenter.I.getPushListener().onOffline(message);

            Logs.PUSH.info("[SingleUserPush] remote router not exists user offline, message={}", message);

            return;
        }

        //2.如果查出的远程机器是当前机器，说明路由已经失效，此时用户已下线，需要删除失效的缓存
        if (remoteRouter.getRouteValue().isThisPC(GS_NODE.getIp(), GS_NODE.getPort())) {

            PushCenter.I.getPushListener().onOffline(message);

            //删除失效的远程缓存
            RouterCenter.I.getRemoteRouterManager().unRegister(userId, clientType);

            Logs.PUSH.info("[SingleUserPush] find remote router in this pc, but local router not exists, userId={}, clientType={}, router={}"
                    , userId, clientType, remoteRouter);

            return;
        }

        //3.否则说明用户已经跑到另外一台机器上了；路由信息发生更改，让PushClient重推
        PushCenter.I.getPushListener().onRedirect(message);

        Logs.PUSH.info("[SingleUserPush] find router in another pc, userId={}, clientType={}, router={}", userId, clientType, remoteRouter);

    }

    private static final class PushFutureListener implements ChannelFutureListener {
        private final IPushMessage message;
        private final int messageId;

        private PushFutureListener(IPushMessage message, int messageId) {
            this.message = message;
            this.messageId = messageId;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {//推送成功

                if (message.isNeedAck()) {//需要客户端ACK, 添加等待客户端响应ACK的任务
                    addAckTask(messageId);
                } else {
                    PushCenter.I.getPushListener().onSuccess(message);
                }

                Logs.PUSH.info("[SingleUserPush] push message to client success, message={}", message);

            } else {//推送失败

                PushCenter.I.getPushListener().onFailure(message);

                Logs.PUSH.error("[SingleUserPush] push message to client failure, message={}, conn={}", message, future.channel());
            }
        }

        /**
         * 添加ACK任务到队列, 等待客户端响应
         *
         * @param messageId 下发到客户端待ack的消息的sessionId
         */
        private void addAckTask(int messageId) {
            //因为要进队列，可以提前释放一些比较占用内存的字段，便于垃圾回收
            message.finalized();

            AckTask task = AckTask
                    .from(messageId)
                    .setCallback(new PushAckCallback(message));
            AckTaskQueue.I.add(task, message.getTimeoutMills());
        }
    }
}
