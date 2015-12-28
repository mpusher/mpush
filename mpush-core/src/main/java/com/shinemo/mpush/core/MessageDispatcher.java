package com.shinemo.mpush.core;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.Receiver;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.handler.*;
import com.shinemo.mpush.core.message.NettyRequest;

/**
 * Created by ohun on 2015/12/22.
 */
public class MessageDispatcher implements Receiver {
    public static final MessageHandler LOGIN_HANDLER = new LoginHandler();
    public static final MessageHandler BIND_HANDLER = new BindHandler();
    public static final MessageHandler HEART_HANDLER = new HeartBeatHandler();
    public static final MessageHandler HAND_SHAKE_HANDLER = new HandShakeHandler();
    public static final MessageHandler FAST_CONNECT_HANDLER = new FastConnectHandler();

    @Override
    public void onMessage(Packet packet, Connection connection) {
        Request request = new NettyRequest(packet, connection);
        switch (request.getCommand()) {
            case Heartbeat:
                HEART_HANDLER.handle(request);
                break;
            case Handshake:
                HAND_SHAKE_HANDLER.handle(request);
                break;
            case Login:
                LOGIN_HANDLER.handle(request);
                break;
            case Bind:
                BIND_HANDLER.handle(request);
                break;
            case Kick:
                break;
            case FastConnect:
                FAST_CONNECT_HANDLER.handle(request);
                break;
            case Unknown:
                break;
        }
    }
}
