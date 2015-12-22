package com.shinemo.mpush.api;

import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public class Response {
    private final Packet packet;
    private final Connection connection;

    public Response(Packet packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
    }

    public void send(byte[] body) {
        packet.body = body;
        connection.send(packet);
    }


    public void sendError(byte[] reson) {

    }
}
