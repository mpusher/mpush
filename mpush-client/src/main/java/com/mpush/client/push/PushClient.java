package com.mpush.client.push;

import com.google.common.base.Strings;
import com.mpush.api.BaseService;
import com.mpush.api.connection.Connection;
import com.mpush.api.push.PushSender;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.client.gateway.GatewayClientFactory;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.zk.listener.ZKServerNodeWatcher;

import java.util.Collection;

/*package*/ class PushClient extends BaseService implements PushSender {
    private static final int DEFAULT_TIMEOUT = 3000;
    private final GatewayClientFactory factory = GatewayClientFactory.I;

    public void send(String content, Collection<String> userIds, Callback callback) {
        if (Strings.isNullOrEmpty(content)) return;
        for (String userId : userIds) {
            PushRequest
                    .build(this)
                    .setCallback(callback)
                    .setUserId(userId)
                    .setContent(content)
                    .setTimeout(DEFAULT_TIMEOUT)
                    .send();
        }
    }

    @Override
    public void send(String content, String userId, Callback callback) {
        PushRequest
                .build(this)
                .setCallback(callback)
                .setUserId(userId)
                .setContent(content)
                .setTimeout(DEFAULT_TIMEOUT)
                .send();
    }

    public Connection getGatewayConnection(String host) {
        return factory.getConnection(host);
    }

    @Override
    public void start(Listener listener) {
        if (started.compareAndSet(false, true)) {
            ZKClient.I.init();
            RedisManager.I.init();
            ZKServerNodeWatcher.build(ZKPath.GATEWAY_SERVER, factory).beginWatch();
            if (listener != null) {
                listener.onSuccess(0);
            }
        }
    }

    @Override
    public void stop(Listener listener) {
        if (started.compareAndSet(true, false)) {
            factory.clear();
        }
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }
}
