package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public final class HeartBeatHandler implements MessageHandler {
    @Override
    public void handle(Packet packet, Connection connection) {
        connection.send(packet);
    }
}
