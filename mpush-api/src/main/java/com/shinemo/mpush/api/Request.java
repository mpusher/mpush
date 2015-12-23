package com.shinemo.mpush.api;

import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public class Request {
    private final Command command;
    private final Packet message;
    private final Connection connection;

    public Request(Packet message, Connection connection) {
        this.message = message;
        this.connection = connection;
        this.command = Command.toCMD(message.command);
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
        packet.command = message.command;
        packet.msgId = message.msgId;
        packet.version = message.version;
        packet.msgType = message.msgType;
        return new Response(packet, connection);
    }
}
