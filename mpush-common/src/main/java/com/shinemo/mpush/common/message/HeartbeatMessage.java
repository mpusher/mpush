package com.shinemo.mpush.common.message;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.Message;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.channel.ChannelFutureListener;

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
    public void send(ChannelFutureListener listener) {

    }

    @Override
    public void sendRaw(ChannelFutureListener listener) {

    }

    @Override
    public Packet getPacket() {
        return null;
    }
}
