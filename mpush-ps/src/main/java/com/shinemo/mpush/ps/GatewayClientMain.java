package com.shinemo.mpush.ps;

import java.util.Collection;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.PushSender.Callback;
import com.shinemo.mpush.common.AbstractClient;
import com.shinemo.mpush.ps.zk.listener.impl.GatewayServerPathListener;

public class GatewayClientMain extends AbstractClient {

    private static final int defaultTimeout = 3000;

    public GatewayClientMain() {
        registerListener(new GatewayServerPathListener());
    }

    public void send(String content, Collection<String> userIds, Callback callback) {
        if (Strings.isNullOrEmpty(content)) return;
        for (String userId : userIds) {
            PushRequest
                    .build()
                    .setCallback(callback)
                    .setUserId(userId)
                    .setContent(content)
                    .setTimeout(defaultTimeout)
                    .send();
        }
    }

}
