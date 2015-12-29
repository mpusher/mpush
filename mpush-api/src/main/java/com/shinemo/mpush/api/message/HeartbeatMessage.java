package com.shinemo.mpush.api.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Message;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/28.
 */
public final class HeartbeatMessage implements Message {
    private final Connection connection;

    public HeartbeatMessage(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void send() {

    }

    @Override
    public void sendRaw() {

    }

    @Override
    public Packet getPacket() {
        return null;
    }
}
