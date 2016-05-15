package com.mpush.api;

import com.mpush.api.protocol.Packet;
import com.mpush.api.connection.Connection;

/**
 * Created by ohun on 2015/12/22.
 */
public interface PacketReceiver {
    void onReceive(Packet packet, Connection connection);
}
