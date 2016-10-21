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
import com.mpush.client.gateway.GatewayConnectionFactory;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.router.CachedRemoteRouterManager;
import com.mpush.common.router.RemoteRouter;
import com.mpush.tools.Jsons;
import com.mpush.tools.common.TimeLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public class PushRequest extends FutureTask<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushRequest.class);

    private static final Callable<Boolean> NONE = () -> Boolean.FALSE;

    private enum Status {init, success, failure, offline, timeout}

    private final AtomicReference<Status> status = new AtomicReference<>(Status.init);
    private final TimeLine timeLine = new TimeLine("Push-Time-Line");

    private final GatewayConnectionFactory connectionFactory;

    private AckModel ackModel;
    private Set<String> tags;
    private PushCallback callback;
    private String userId;
    private byte[] content;
    private int timeout;
    private ClientLocation location;
    private Future<?> future;
    private String result;

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

        timeLine.addTimePoint("get-gateway-conn");


        //2.通过网关连接，把消息发送到所在机器

        timeLine.addTimePoint("send-to-gateway-begin");

        boolean success = connectionFactory.send(location.getHost(), new Consumer<GatewayPushMessage>() {
            @Override
            public void accept(GatewayPushMessage pushMessage) {
                pushMessage
                        .setUserId(userId)
                        .setContent(content)
                        .setClientType(location.getClientType())
                        .setTimeout(timeout - 500)
                        .setTags(tags)
                        .addFlag(ackModel.flag);

                pushMessage.sendRaw(f -> {
                    timeLine.addTimePoint("send-to-gateway-end");
                    if (!f.isSuccess()) failure();
                });
                PushRequest.this.content = null;//释放内存
                timeLine.addTimePoint("put-request-bus");
                future = PushRequestBus.I.put(pushMessage.getSessionId(), PushRequest.this);
            }
        });

        if (!success) {
            LOGGER.error("get gateway connection failure, location={}", location);
            failure();
        }
    }

    private void submit(Status status) {
        if (this.status.compareAndSet(Status.init, status)) {//防止重复调用
            if (future != null) future.cancel(true);
            if (callback != null) {
                PushRequestBus.I.asyncCall(this);
            } else {
                LOGGER.warn("callback is null");
            }
            super.set(this.status.get() == Status.success);
        }
        timeLine.end();
        LOGGER.info("push request {} end, userId={}, content={}, location={}, timeLine={}"
                , status, userId, content, location, timeLine);
    }

    @Override
    public void run() {
        if (status.get() == Status.init) {//从定时任务过来的，超时时间到了
            submit(Status.timeout);
        } else {
            callback.onResult(new PushResult(status.get().ordinal())
                    .setUserId(userId)
                    .setUserIds(userId == null ? Jsons.fromJson(result, String[].class) : null)
                    .setLocation(location)
                    .setTimeLine(timeLine.getTimePoints())
            );
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public FutureTask<Boolean> send(RemoteRouter router) {
        timeLine.begin();
        sendToConnServer(router);
        return this;
    }

    public void redirect() {
        timeLine.addTimePoint("redirect");
        LOGGER.warn("user route has changed, userId={}, location={}", userId, location);
        CachedRemoteRouterManager.I.invalidateLocalCache(userId);
        if (status.get() == Status.init) {//表示任务还没有完成，还可以重新发送
            RemoteRouter remoteRouter = CachedRemoteRouterManager.I.lookup(userId, location.getClientType());
            send(remoteRouter);
        }
    }

    public FutureTask<Boolean> offline() {
        CachedRemoteRouterManager.I.invalidateLocalCache(userId);
        submit(Status.offline);
        return this;
    }

    public FutureTask<Boolean> broadcast() {
        timeLine.begin();

        Consumer<GatewayPushMessage> consumer = pushMessage -> {
            pushMessage
                    .setUserId(userId)
                    .setContent(content)
                    .setTags(tags)
                    .addFlag(ackModel.flag);

            pushMessage.sendRaw(f -> {
                if (!f.isSuccess()) failure();
            });

            future = PushRequestBus.I.put(pushMessage.getSessionId(), PushRequest.this);
        };

        connectionFactory.broadcast(consumer);
        return this;
    }

    public void timeout() {
        submit(Status.timeout);
    }

    public void success(String data) {
        this.result = data;
        submit(Status.success);
    }

    public void failure() {
        submit(Status.failure);
    }

    public long getTimeout() {
        return timeout;
    }

    public PushRequest(GatewayConnectionFactory factory) {
        super(NONE);
        this.connectionFactory = factory;
    }

    public static PushRequest build(GatewayConnectionFactory factory, PushContext ctx) {
        byte[] content = ctx.getContext();
        PushMsg msg = ctx.getPushMsg();
        if (msg != null) {
            String json = Jsons.toJson(msg);
            if (json != null) {
                content = json.getBytes(Constants.UTF_8);
            }
        }
        return new PushRequest(factory)
                .setAckModel(ctx.getAckModel())
                .setUserId(ctx.getUserId())
                .setTags(ctx.getTags())
                .setContent(content)
                .setTimeout(ctx.getTimeout())
                .setCallback(ctx.getCallback());

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

    @Override
    public String toString() {
        return "PushRequest{" +
                "content='" + content + '\'' +
                ", userId='" + userId + '\'' +
                ", timeout=" + timeout +
                ", location=" + location +
                '}';
    }
}
