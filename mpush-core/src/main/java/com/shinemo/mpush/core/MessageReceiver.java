package com.shinemo.mpush.core;

import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.Receiver;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.core.handler.LoginMessageHandler;

/**
 * Created by ohun on 2015/12/22.
 */
public class MessageReceiver implements Receiver {
    public static final MessageHandler LOGIN_MESSAGE_HANDLER = new LoginMessageHandler();

    @Override
    public void onMessage(Request request) {
        switch (request.getCommand()) {
            case Heartbeat:
                break;
            case Handshake:
                break;
            case Login:
                LOGIN_MESSAGE_HANDLER.handle(request);
                break;
            case Kick:
                break;
            case Unknown:
                break;
        }
    }
}
