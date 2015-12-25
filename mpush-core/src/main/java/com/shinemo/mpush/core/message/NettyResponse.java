package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Constants;
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
        packet.body = DESUtils.decryptDES(body, connection.getSessionInfo().desKey);
        connection.send(packet);
    }

    public void sendRaw(byte[] body) {
        packet.body = body;
        connection.send(packet);
    }


    public void sendError(byte[] reason) {
        packet.body = reason;
        connection.send(packet);
    }

    @Override
    public void send(String body) {
        send(body.getBytes(Constants.UTF_8));
    }

    @Override
    public void sendRaw(String body) {
        sendRaw(body.getBytes(Constants.UTF_8));
    }

    @Override
    public void sendError(String reason) {
        sendError(reason.getBytes(Constants.UTF_8));
    }
}
