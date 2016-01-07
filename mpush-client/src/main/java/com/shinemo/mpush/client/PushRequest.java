package com.shinemo.mpush.client;

import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.common.message.gateway.GatewayPushMessage;
import com.shinemo.mpush.common.router.ConnectionRouterManager;
import com.shinemo.mpush.common.router.RemoteRouter;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushRequest implements PushSender.Callback, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushRequest.class);
    private PushSender.Callback callback;
    private String userId;
    private String content;
    private long timeout;
    private PushClient pushClient;
    private int status = 0;
    private long timeout_;
    private int sessionId;
    private long sendTime;

    public PushRequest(PushClient pushClient) {
        this.pushClient = pushClient;
    }

    public static PushRequest build(PushClient pushClient) {
        return new PushRequest(pushClient);
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

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    @Override
    public void onSuccess(String userId) {
        submit(1);
    }

    @Override
    public void onFailure(String userId) {
        submit(2);
    }

    @Override
    public void onOffline(String userId) {
        submit(3);
    }

    @Override
    public void onTimeout(String userId) {
        submit(4);
    }

    private void submit(int status) {
        this.status = status;
        if (sessionId > 0) PushRequestBus.INSTANCE.remove(sessionId);
        if (callback != null) {
            PushRequestBus.INSTANCE.getExecutor().execute(this);
        } else {
            LOGGER.warn("callback is null");
        }
    }

    @Override
    public void run() {
        switch (status) {
            case 1:
                callback.onSuccess(userId);
            case 2:
                callback.onFailure(userId);
            case 3:
                callback.onOffline(userId);
            case 4:
                callback.onTimeout(userId);
        }
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() > timeout_;
    }

    public void timeout() {
        onTimeout(userId);
    }

    public void success() {
        onSuccess(userId);
    }

    public void failure() {
        onFailure(userId);
    }

    public void offline() {
        onOffline(userId);
    }

    public void send() {
        this.timeout_ = timeout + System.currentTimeMillis();
        sendToConnectionServer();
    }

    public void redirect() {
        this.timeout_ = timeout + System.currentTimeMillis();
        ConnectionRouterManager.INSTANCE.invalidateLocalCache(userId);
        sendToConnectionServer();
        LOGGER.warn("user route has changed, userId={}, content={}", userId, content);
    }

    private void sendToConnectionServer() {
        RemoteRouter router = ConnectionRouterManager.INSTANCE.lookup(userId);
        if (router == null) {
            this.onOffline(userId);
            return;
        }

        ClientLocation location = router.getRouteValue();
        Connection connection = pushClient.getConnection(location.getHost());
        if (connection == null || !connection.isConnected()) {
            this.onFailure(userId);
            return;
        }

        GatewayPushMessage pushMessage = new GatewayPushMessage(userId, content, connection);
        pushMessage.send(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    sendTime = System.currentTimeMillis();
                } else {
                    PushRequest.this.onFailure(userId);
                }
            }
        });

        this.sessionId = pushMessage.getSessionId();
        PushRequestBus.INSTANCE.add(this);
    }
}
