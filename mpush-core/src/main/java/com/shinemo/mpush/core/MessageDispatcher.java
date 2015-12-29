package com.shinemo.mpush.core;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.handler.*;
import com.shinemo.mpush.api.message.BindUserMessage;
import com.shinemo.mpush.api.message.FastConnectMessage;
import com.shinemo.mpush.api.message.HandShakeMessage;
import com.shinemo.mpush.api.message.HeartbeatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/22.
 */
public class MessageDispatcher implements PacketReceiver {
    public static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatcher.class);
    public final BindHandler bindHandler = new BindHandler();
    public final HandShakeHandler handShakeHandler = new HandShakeHandler();
    public final FastConnectHandler fastConnectHandler = new FastConnectHandler();
    public final HeartBeatHandler heartBeatHandler = new HeartBeatHandler();

    @Override
    public void onReceive(Packet packet, Connection connection) {
        Command command = Command.toCMD(packet.cmd);
        try {
            switch (command) {
                case HEARTBEAT:
                    heartBeatHandler.handle(new HeartbeatMessage(connection));
                    break;
                case HANDSHAKE:
                    handShakeHandler.handle(new HandShakeMessage(packet, connection));
                    break;
                case BIND:
                    bindHandler.handle(new BindUserMessage(packet, connection));
                    break;
                case FAST_CONNECT:
                    fastConnectHandler.handle(new FastConnectMessage(packet, connection));
                    break;
                case UNKNOWN:
                    break;
            }
        } catch (Throwable throwable) {
            LOGGER.error("dispatch packet ex, packet={},conn={}", packet, connection, throwable);
            connection.close();
        }
    }
}
