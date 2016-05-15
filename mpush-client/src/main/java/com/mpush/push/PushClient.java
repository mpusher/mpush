package com.mpush.push;

import com.google.common.base.Strings;
import com.mpush.api.PushSender;
import com.mpush.api.connection.Connection;
import com.mpush.common.AbstractClient;
import com.mpush.push.zk.listener.GatewayZKListener;

import java.util.Collection;

public class PushClient extends AbstractClient implements PushSender {
    private static final int DEFAULT_TIMEOUT = 3000;
    private final GatewayZKListener listener = new GatewayZKListener();

    public PushClient() {
        registerListener(listener);
    }

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

    public Connection getGatewayConnection(String ip) {
        return listener.getManager().getConnection(ip);
    }

}
