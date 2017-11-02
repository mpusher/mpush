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

import com.mpush.api.message.Message;
import com.mpush.api.common.Condition;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.spi.push.IPushMessage;
import com.mpush.common.condition.AwaysPassCondition;
import com.mpush.common.message.PushMessage;
import com.mpush.common.qps.FlowControl;
import com.mpush.common.qps.OverFlowException;
import com.mpush.core.MPushServer;
import com.mpush.core.router.LocalRouter;
import com.mpush.tools.common.TimeLine;
import com.mpush.tools.log.Logs;
import io.netty.channel.ChannelFuture;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class BroadcastPushTask implements PushTask {

    private final long begin = System.currentTimeMillis();

    private final AtomicInteger finishTasks = new AtomicInteger(0);

    private final TimeLine timeLine = new TimeLine();

    private final Set<String> successUserIds = new HashSet<>(1024);

    private final FlowControl flowControl;

    private final IPushMessage message;

    private final Condition condition;

    private final MPushServer mPushServer;

    //使用Iterator, 记录任务遍历到的位置，因为有流控，一次任务可能会被分批发送，而且还有在推送过程中上/下线的用户
    private final Iterator<Map.Entry<String, Map<Integer, LocalRouter>>> iterator;

    public BroadcastPushTask(MPushServer mPushServer, IPushMessage message, FlowControl flowControl) {
        this.mPushServer = mPushServer;
        this.message = message;
        this.flowControl = flowControl;
        this.condition = message.getCondition();
        this.iterator = mPushServer.getRouterCenter().getLocalRouterManager().routers().entrySet().iterator();
        this.timeLine.begin("push-center-begin");
    }

    @Override
    public void run() {
        flowControl.reset();
        boolean done = broadcast();
        if (done) {//done 广播结束
            if (finishTasks.addAndGet(flowControl.total()) == 0) {
                report();
            }
        } else {//没有结束，就延时进行下次任务 TODO 考虑优先级问题
            mPushServer.getPushCenter().delayTask(flowControl.getDelay(), this);
        }
        flowControl.end(successUserIds.toArray(new String[successUserIds.size()]));
    }

    private boolean broadcast() {
        try {
            iterator.forEachRemaining(entry -> {

                String userId = entry.getKey();
                entry.getValue().forEach((clientType, router) -> {

                    Connection connection = router.getRouteValue();

                    if (checkCondition(condition, connection)) {//1.条件检测
                        if (connection.isConnected()) {
                            if (connection.getChannel().isWritable()) { //检测TCP缓冲区是否已满且写队列超过最高阀值
                                PushMessage
                                        .build(connection)
                                        .setContent(message.getContent())
                                        .send(future -> operationComplete(future, userId));
                                //4. 检测qps, 是否超过流控限制，如果超过则结束当前循环直接进入catch
                                if (!flowControl.checkQps()) {
                                    throw new OverFlowException(false);
                                }
                            }
                        } else { //2.如果链接失效，先删除本地失效的路由，再查下远程路由，看用户是否登陆到其他机器
                            Logs.PUSH.warn("[Broadcast] find router in local but conn disconnect, message={}, conn={}", message, connection);
                            //删除已经失效的本地路由
                            mPushServer.getRouterCenter().getLocalRouterManager().unRegister(userId, clientType);
                        }
                    }

                });

            });
        } catch (OverFlowException e) {
            //超出最大限制，或者遍历完毕，结束广播
            return e.isOverMaxLimit() || !iterator.hasNext();
        }
        return !iterator.hasNext();//遍历完毕, 广播结束
    }

    private void report() {
        Logs.PUSH.info("[Broadcast] task finished, cost={}, message={}", (System.currentTimeMillis() - begin), message);
        mPushServer.getPushCenter().getPushListener().onBroadcastComplete(message, timeLine.end().getTimePoints());//通知发送方，广播推送完毕
    }

    private boolean checkCondition(Condition condition, Connection connection) {
        if (condition == AwaysPassCondition.I) return true;
        SessionContext sessionContext = connection.getSessionContext();
        Map<String, Object> env = new HashMap<>();
        env.put("userId", sessionContext.userId);
        env.put("tags", sessionContext.tags);
        env.put("clientVersion", sessionContext.clientVersion);
        env.put("osName", sessionContext.osName);
        env.put("osVersion", sessionContext.osVersion);
        return condition.test(env);
    }

    //@Override
    private void operationComplete(ChannelFuture future, String userId) throws Exception {
        if (future.isSuccess()) {//推送成功
            successUserIds.add(userId);
            Logs.PUSH.info("[Broadcast] push message to client success, userId={}, message={}", message.getUserId(), message);
        } else {//推送失败
            Logs.PUSH.warn("[Broadcast] push message to client failure, userId={}, message={}, conn={}", message.getUserId(), message, future.channel());
        }
        if (finishTasks.decrementAndGet() == 0) {
            report();
        }
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return ((Message) message).getConnection().getChannel().eventLoop();
    }
}
