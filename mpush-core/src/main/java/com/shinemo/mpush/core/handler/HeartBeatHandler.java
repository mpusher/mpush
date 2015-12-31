package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public final class HeartbeatHandler implements MessageHandler {
    @Override
    public void handle(Packet packet, Connection connection) {
        System.err.println("receive client heartbeat, time="
                + System.currentTimeMillis());

    }
}
