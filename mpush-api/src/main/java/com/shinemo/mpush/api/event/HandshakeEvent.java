package com.shinemo.mpush.api.event;

import com.shinemo.mpush.api.Connection;

/**
 * Created by ohun on 2015/12/29.
 */
public class HandshakeEvent implements Event {
    public final Connection connection;
    public final int heartbeat;

    public HandshakeEvent(Connection connection, int heartbeat) {
        this.connection = connection;
        this.heartbeat = heartbeat;
    }
}
