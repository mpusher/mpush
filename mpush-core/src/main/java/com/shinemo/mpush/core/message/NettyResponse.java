package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Response;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.tools.crypto.DESUtils;

/**
 * Created by ohun on 2015/12/22.
 */
public class NettyResponse implements Response {
    private final Packet packet;
    private final Connection connection;

    public NettyResponse(Packet packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
    }

    public void send(byte[] body) {
        packet.body = DESUtils.decryptDES(body, connection.getInfo().desKey);
        connection.send(packet);
    }

    public void sendRaw(byte[] body) {
        packet.body = body;
        connection.send(packet);
    }


    public void sendError(byte[] reason) {

    }
}
