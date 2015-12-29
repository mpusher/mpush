package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.message.HeartbeatMessage;

/**
 * Created by ohun on 2015/12/22.
 */
public final class HeartBeatHandler implements MessageHandler<HeartbeatMessage> {

    @Override
    public void handle(HeartbeatMessage message) {
        System.err.println("receive client heartbeat, time="
                + System.currentTimeMillis());
    }

}
