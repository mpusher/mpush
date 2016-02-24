package com.shinemo.mpush.push;

import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.router.ClientLocation;
import com.shinemo.mpush.common.manage.ServerManage;
import com.shinemo.mpush.common.message.gateway.GatewayPushMessage;
import com.shinemo.mpush.common.router.ConnectionRouterManager;
import com.shinemo.mpush.common.router.RemoteRouter;
import com.shinemo.mpush.push.manage.impl.GatewayServerManage;
import com.shinemo.mpush.tools.spi.ServiceContainer;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushRequest implements PushSender.Callback, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushRequest.class);

    private final static GatewayServerManage gatewayClientManage = (GatewayServerManage) ServiceContainer.getInstance(ServerManage.class, "gatewayServerManage");

    private PushSender.Callback callback;
    private String userId;
    private String content;
    private long timeout;
    private long timeout_;
    private int sessionId;
    private long sendTime;
    private AtomicInteger status = new AtomicInteger(0);

    public PushRequest() {
    }

    public static PushRequest build() {
        return new PushRequest();
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
        if (this.status.compareAndSet(0, status)) {//防止重复调用
            if (callback != null) {
                PushRequestBus.INSTANCE.getExecutor().execute(this);
            } else {
                LOGGER.warn("callback is null");
            }
        }
    }

    @Override
    public void run() {
        switch (status.get()) {
            case 1:
                callback.onSuccess(userId);
                break;
            case 2:
                callback.onFailure(userId);
                break;
            case 3:
                callback.onOffline(userId);
                break;
            case 4:
                callback.onTimeout(userId);
                break;
        }
    }

    public boolean isTimeout() {
    	long delt = System.currentTimeMillis() - timeout_;
    	LOGGER.info("delt:{},content:{}",delt,content);
        return delt > 0 ? true :false;
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
        ConnectionRouterManager.INSTANCE.invalidateLocalCache(userId);
        onOffline(userId);
    }

    public void send() {
        this.timeout_ = timeout + System.currentTimeMillis();
        sendToConnServer();
    }

    public void redirect() {
        ConnectionRouterManager.INSTANCE.invalidateLocalCache(userId);
        LOGGER.warn("user route has changed, userId={}, content={}", userId, content);
        if (status.get() == 0) {
            send();
        }
    }

    private void sendToConnServer() {
        //1.查询用户长连接所在的机器
        RemoteRouter router = ConnectionRouterManager.INSTANCE.lookup(userId);
        if (router == null) {
            //1.1没有查到说明用户已经下线
            this.onOffline(userId);
            return;
        }

        //2.通过网关连接，把消息发送到所在机器
        ClientLocation location = router.getRouteValue();
        Connection gatewayConn = gatewayClientManage.getConnection(location.getHost());
        if (gatewayConn == null || !gatewayConn.isConnected()) {
            this.onFailure(userId);
            return;
        }

        final GatewayPushMessage pushMessage = new GatewayPushMessage(userId, content, gatewayConn);
        pushMessage.sendRaw(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    sendTime = System.currentTimeMillis();
                    LOGGER.warn("pushMessage packet:" + pushMessage.getPacket());
                } else {
                    PushRequest.this.onFailure(userId);
                }
            }
        });

        sessionId = pushMessage.getSessionId();
        PushRequestBus.INSTANCE.put(sessionId, this);
    }

    public long getSendTime() {
        return sendTime;
    }

}
