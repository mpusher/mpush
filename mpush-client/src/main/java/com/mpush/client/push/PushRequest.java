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

import com.mpush.api.connection.Connection;
import com.mpush.api.push.PushSender;
import com.mpush.api.router.ClientLocation;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.router.ConnectionRouterManager;
import com.mpush.common.router.RemoteRouter;
import com.mpush.tools.common.TimeLine;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public class PushRequest extends FutureTask<Boolean> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushRequest.class);

    private enum Status {init, success, failure, offline, timeout}

    private static final Callable<Boolean> NONE = () -> Boolean.FALSE;

    private final AtomicReference<Status> status = new AtomicReference<>(Status.init);
    private final TimeLine timeLine = new TimeLine("Push-Time-Line");

    private final PushClient client;

    private PushSender.Callback callback;
    private String userId;
    private String content;
    private long timeout;
    private ClientLocation location;
    private Future<?> future;

    private void sendToConnServer() {
        timeLine.addTimePoint("lookup-remote");

        //1.查询用户长连接所在的机器
        RemoteRouter router = ConnectionRouterManager.INSTANCE.lookup(userId);
        if (router == null) {
            //1.1没有查到说明用户已经下线
            offline();
            return;
        }

        timeLine.addTimePoint("get-gateway-conn");


        //2.通过网关连接，把消息发送到所在机器
        location = router.getRouteValue();
        Connection gatewayConn = client.getGatewayConnection(location.getHost());
        if (gatewayConn == null) {
            LOGGER.error("get gateway connection failure, location={}", location);
            failure();
            return;
        }

        timeLine.addTimePoint("send-to-gateway-begin");

        GatewayPushMessage pushMessage = new GatewayPushMessage(userId, content, gatewayConn);
        timeLine.addTimePoint("put-request-bus");
        future = PushRequestBus.I.put(pushMessage.getSessionId(), this);

        pushMessage.sendRaw(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                timeLine.addTimePoint("send-to-gateway-end");
                if (future.isSuccess()) {
                } else {
                    failure();
                }
            }
        });
    }

    private void submit(Status status) {
        if (this.status.compareAndSet(Status.init, status)) {//防止重复调用
            if (future != null) future.cancel(true);
            if (callback != null) {
                PushRequestBus.I.asyncCall(this);
            } else {
                LOGGER.warn("callback is null");
            }
        }
        timeLine.end();
        LOGGER.info("push request {} end, userId={}, content={}, location={}, timeLine={}"
                , status, userId, content, location, timeLine);
    }

    @Override
    public void run() {
        switch (status.get()) {
            case success:
                callback.onSuccess(userId);
                break;
            case failure:
                callback.onFailure(userId);
                break;
            case offline:
                callback.onOffline(userId);
                break;
            case timeout:
                callback.onTimeout(userId);
                break;
            case init://从定时任务过来的，超时时间到了
                submit(Status.timeout);
                break;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public void send() {
        timeLine.begin();
        sendToConnServer();
    }

    public void redirect() {
        timeLine.addTimePoint("redirect");
        LOGGER.warn("user route has changed, userId={}, location={}", userId, location);
        ConnectionRouterManager.INSTANCE.invalidateLocalCache(userId);
        if (status.get() == Status.init) {//表示任务还没有完成，还可以重新发送
            send();
        }
    }

    public long getTimeout() {
        return timeout;
    }

    public void timeout() {
        submit(Status.timeout);
    }

    public void success() {
        submit(Status.success);
    }

    public void failure() {
        submit(Status.failure);
    }

    public void offline() {
        ConnectionRouterManager.INSTANCE.invalidateLocalCache(userId);
        submit(Status.offline);
    }

    public PushRequest(PushClient client) {
        super(NONE);
        this.client = client;
    }

    public static PushRequest build(PushClient client) {
        return new PushRequest(client);
    }

    public PushRequest setCallback(PushSender.Callback callback) {
        this.callback = callback;
        return this;
    }

    public PushRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public PushRequest setContent(String content) {
        this.content = content;
        return this;
    }

    public PushRequest setTimeout(long timeout) {
        this.timeout = timeout;
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
