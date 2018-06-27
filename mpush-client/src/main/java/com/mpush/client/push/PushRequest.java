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

package com.mpush.client.push;

import com.mpush.api.Constants;
import com.mpush.api.push.*;
import com.mpush.api.router.ClientLocation;
import com.mpush.client.MPushClient;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.push.GatewayPushResult;
import com.mpush.common.router.RemoteRouter;
import com.mpush.tools.Jsons;
import com.mpush.tools.common.TimeLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public final class PushRequest extends FutureTask<PushResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushRequest.class);

    private static final Callable<PushResult> NONE = () -> new PushResult(PushResult.CODE_FAILURE);

    private enum Status {init, success, failure, offline, timeout}

    private final AtomicReference<Status> status = new AtomicReference<>(Status.init);
    private final TimeLine timeLine = new TimeLine("Push-Time-Line");

    private final MPushClient mPushClient;

    private AckModel ackModel;
    private Set<String> tags;
    private String condition;
    private PushCallback callback;
    private String userId;
    private byte[] content;
    private int timeout;
    private ClientLocation location;
    private int sessionId;
    private String taskId;
    private Future<?> future;
    private PushResult result;

    private void sendToConnServer(RemoteRouter remoteRouter) {
        timeLine.addTimePoint("lookup-remote");

        if (remoteRouter != null) {
            location = remoteRouter.getRouteValue();
        }

        if (remoteRouter == null || remoteRouter.isOffline()) {
            //1.1没有查到说明用户已经下线
            offline();
            return;
        }

        timeLine.addTimePoint("check-gateway-conn");
        //2.通过网关连接，把消息发送到所在机器
        boolean success = mPushClient.getGatewayConnectionFactory().send(
                location.getHostAndPort(),
                connection -> GatewayPushMessage
                        .build(connection)
                        .setUserId(userId)
                        .setContent(content)
                        .setClientType(location.getClientType())
                        .setTimeout(timeout - 500)
                        .setTags(tags)
                        .addFlag(ackModel.flag)
                ,
                pushMessage -> {
                    timeLine.addTimePoint("send-to-gateway-begin");
                    pushMessage.sendRaw(f -> {
                        timeLine.addTimePoint("send-to-gateway-end");
                        if (f.isSuccess()) {
                            LOGGER.debug("send to gateway server success, location={}, conn={}", location, f.channel());
                        } else {
                            LOGGER.error("send to gateway server failure, location={}, conn={}", location, f.channel(), f.cause());
                            failure();
                        }
                    });
                    PushRequest.this.content = null;//释放内存
                    sessionId = pushMessage.getSessionId();
                    future = mPushClient.getPushRequestBus().put(sessionId, PushRequest.this);
                }
        );

        if (!success) {
            LOGGER.error("get gateway connection failure, location={}", location);
            failure();
        }
    }

    private void submit(Status status) {
        if (this.status.compareAndSet(Status.init, status)) {//防止重复调用
            boolean isTimeoutEnd = status == Status.timeout;//任务是否超时结束

            if (future != null && !isTimeoutEnd) {//是超时结束任务不用再取消一次
                future.cancel(true);//取消超时任务
            }

            this.timeLine.end();//结束时间流统计
            super.set(getResult());//设置同步调用的返回结果

            if (callback != null) {//回调callback
                if (isTimeoutEnd) {//超时结束时，当前线程已经是线程池里的线程，直接调用callback
                    callback.onResult(getResult());
                } else {//非超时结束时，当前线程为Netty线程池，要异步执行callback
                    mPushClient.getPushRequestBus().asyncCall(this);//会执行run方法
                }
            }
        }
        LOGGER.info("push request {} end, {}, {}, {}", status, userId, location, timeLine);
    }

    /**
     * run方法会有两个地方的线程调用
     * 1. 任务超时时会调用，见PushRequestBus.I.put(sessionId, PushRequest.this);
     * 2. 异步执行callback的时候，见PushRequestBus.I.asyncCall(this);
     */
    @Override
    public void run() {
        //判断任务是否超时，如果超时了此时状态是init，否则应该是其他状态, 因为从submit方法过来的状态都不是init
        if (status.get() == Status.init) {
            timeout();
        } else {
            callback.onResult(getResult());
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public FutureTask<PushResult> send(RemoteRouter router) {
        timeLine.begin();
        sendToConnServer(router);
        return this;
    }

    public FutureTask<PushResult> broadcast() {
        timeLine.begin();

        boolean success = mPushClient.getGatewayConnectionFactory()
                .broadcast(
                        connection -> GatewayPushMessage
                                .build(connection)
                                .setUserId(userId)
                                .setContent(content)
                                .setTags(tags)
                                .setCondition(condition)
                                .setTaskId(taskId)
                                .addFlag(ackModel.flag),

                        pushMessage -> {
                            pushMessage.sendRaw(f -> {
                                if (f.isSuccess()) {
                                    LOGGER.debug("send broadcast to gateway server success, userId={}, conn={}", userId, f.channel());
                                } else {
                                    failure();
                                    LOGGER.error("send broadcast to gateway server failure, userId={}, conn={}", userId, f.channel(), f.cause());
                                }
                            });

                            if (pushMessage.taskId == null) {
                                sessionId = pushMessage.getSessionId();
                                future = mPushClient.getPushRequestBus().put(sessionId, PushRequest.this);
                            } else {
                                success();
                            }
                        }
                );

        if (!success) {
            LOGGER.error("get gateway connection failure when broadcast.");
            failure();
        }

        return this;
    }

    private void offline() {
        mPushClient.getCachedRemoteRouterManager().invalidateLocalCache(userId);
        submit(Status.offline);
    }

    private void timeout() {
        //超时要把request从队列中移除，其他情况是XXHandler中移除的
        if (mPushClient.getPushRequestBus().getAndRemove(sessionId) != null) {
            submit(Status.timeout);
        }
    }

    private void success() {
        submit(Status.success);
    }

    private void failure() {
        submit(Status.failure);
    }

    public void onFailure() {
        failure();
    }

    public void onRedirect() {
        timeLine.addTimePoint("redirect");
        LOGGER.warn("user route has changed, userId={}, location={}", userId, location);
        //1. 清理一下缓存，确保查询的路由是正确的
        mPushClient.getCachedRemoteRouterManager().invalidateLocalCache(userId);
        if (status.get() == Status.init) {//init表示任务还没有完成，还可以重新发送
            //2. 取消前一次任务, 否则会有两次回调
            if (mPushClient.getPushRequestBus().getAndRemove(sessionId) != null) {
                if (future != null && !future.isCancelled()) {
                    future.cancel(true);
                }
            }
            //3. 取最新的路由重发一次
            send(mPushClient.getCachedRemoteRouterManager().lookup(userId, location.getClientType()));
        }
    }

    public FutureTask<PushResult> onOffline() {
        offline();
        return this;
    }

    public void onSuccess(GatewayPushResult result) {
        if (result != null) timeLine.addTimePoints(result.timePoints);
        submit(Status.success);
    }

    public long getTimeout() {
        return timeout;
    }

    public PushRequest(MPushClient mPushClient) {
        super(NONE);
        this.mPushClient = mPushClient;
    }

    public static PushRequest build(MPushClient mPushClient, PushContext ctx) {
        byte[] content = ctx.getContext();
        PushMsg msg = ctx.getPushMsg();
        if (msg != null) {
            String json = Jsons.toJson(msg);
            if (json != null) {
                content = json.getBytes(Constants.UTF_8);
            }
        }

        Objects.requireNonNull(content, "push content can not be null.");

        return new PushRequest(mPushClient)
                .setAckModel(ctx.getAckModel())
                .setUserId(ctx.getUserId())
                .setTags(ctx.getTags())
                .setCondition(ctx.getCondition())
                .setTaskId(ctx.getTaskId())
                .setContent(content)
                .setTimeout(ctx.getTimeout())
                .setCallback(ctx.getCallback());

    }

    private PushResult getResult() {
        if (result == null) {
            result = new PushResult(status.get().ordinal())
                    .setUserId(userId)
                    .setLocation(location)
                    .setTimeLine(timeLine.getTimePoints());
        }
        return result;
    }

    public PushRequest setCallback(PushCallback callback) {
        this.callback = callback;
        return this;
    }

    public PushRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public PushRequest setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public PushRequest setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public PushRequest setAckModel(AckModel ackModel) {
        this.ackModel = ackModel;
        return this;
    }

    public PushRequest setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public PushRequest setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public PushRequest setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    @Override
    public String toString() {
        return "PushRequest{" +
                "content='" + (content == null ? -1 : content.length) + '\'' +
                ", userId='" + userId + '\'' +
                ", timeout=" + timeout +
                ", location=" + location +
                '}';
    }
}
