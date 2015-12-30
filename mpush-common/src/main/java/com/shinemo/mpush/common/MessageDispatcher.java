package com.shinemo.mpush.common;

import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohun on 2015/12/22.
 */
public class MessageDispatcher implements PacketReceiver {
    public static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatcher.class);
    private final Map<Byte, MessageHandler> handlers = new HashMap<>();

    public void register(Command command, MessageHandler handler) {
        handlers.put(command.cmd, handler);
    }


    @Override
    public void onReceive(Packet packet, Connection connection) {
        try {
            MessageHandler handler = handlers.get(packet.cmd);
            if (handler != null) {
                handler.handle(packet, connection);
            }
        } catch (Throwable throwable) {
            LOGGER.error("dispatch packet ex, packet={},conn={}", packet, connection, throwable);
            connection.close();
        }
    }
}
