package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.core.message.HeartbeatMessage;

/**
 * Created by ohun on 2015/12/22.
 */
public class HeartBeatHandler extends BaseMessageHandler<HeartbeatMessage> {

    @Override
    public void handle(HeartbeatMessage message) {
        System.err.println("receive client heartbeat, time=" + System.currentTimeMillis());
    }

}
