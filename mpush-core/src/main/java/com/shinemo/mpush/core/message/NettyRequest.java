package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.Response;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public class NettyRequest implements Request{
    private final Command command;
    private final Packet message;
    private final Connection connection;

    public NettyRequest(Packet message, Connection connection) {
        this.message = message;
        this.connection = connection;
        this.command = Command.toCMD(message.cmd);
    }

    public Command getCommand() {
        return command;
    }

    public Packet getMessage() {
        return message;
    }

    public Connection getConnection() {
        return connection;
    }

    public Response getResponse() {
        Packet packet = new Packet();
        packet.cmd = message.cmd;
        packet.sessionId = message.sessionId;
        return new NettyResponse(packet, connection);
    }
}
