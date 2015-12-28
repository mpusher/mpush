package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Message;
import com.shinemo.mpush.api.protocol.Command;

/**
 * Created by ohun on 2015/12/28.
 */
public class HeartbeatMessage implements Message {
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
}
