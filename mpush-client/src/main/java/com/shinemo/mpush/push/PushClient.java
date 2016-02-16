package com.shinemo.mpush.push;

import java.util.Collection;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.common.AbstractClient;
import com.shinemo.mpush.push.zk.listener.impl.GatewayServerPathListener;

public class PushClient extends AbstractClient implements PushSender{

    private static final int defaultTimeout = 3000;

    public PushClient() {
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

	@Override
	public void send(String content, String userId, Callback callback) {
		PushRequest
        .build()
        .setCallback(callback)
        .setUserId(userId)
        .setContent(content)
        .setTimeout(defaultTimeout)
        .send();
	}

}
