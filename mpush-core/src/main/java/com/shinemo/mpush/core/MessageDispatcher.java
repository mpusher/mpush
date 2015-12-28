package com.shinemo.mpush.core;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.Receiver;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.handler.*;
import com.shinemo.mpush.core.message.HandShakeMessage;
import com.shinemo.mpush.core.message.HeartbeatMessage;

/**
 * Created by ohun on 2015/12/22.
 */
public class MessageDispatcher implements Receiver {
    public static final MessageHandler BIND_HANDLER = new BindHandler();
    public static final HeartBeatHandler HEART_HANDLER = new HeartBeatHandler();
    public static final HandShakeHandler HAND_SHAKE_HANDLER = new HandShakeHandler();
    public static final FastConnectHandler FAST_CONNECT_HANDLER = new FastConnectHandler();

    @Override
    public void onMessage(Packet packet, Connection connection) {
        Command command = Command.toCMD(packet.cmd);
        switch (command) {
            case Heartbeat:
                HEART_HANDLER.handle(new HeartbeatMessage(connection));
                break;
            case Handshake:
                HAND_SHAKE_HANDLER.handle(new HandShakeMessage(packet, connection));
                break;
            case Login:
                break;
            case Bind:
                BIND_HANDLER.handle(null);
                break;
            case Kick:
                break;
            case FastConnect:
                FAST_CONNECT_HANDLER.handle(null);
                break;
            case Unknown:
                break;
        }
    }
}
