package com.shinemo.mpush.core;

import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.Receiver;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.core.handler.BindHandler;
import com.shinemo.mpush.core.handler.LoginHandler;

/**
 * Created by ohun on 2015/12/22.
 */
public class MessageReceiver implements Receiver {
    public static final MessageHandler LOGIN_HANDLER = new LoginHandler();
    public static final MessageHandler BIND_HANDLER = new BindHandler();

    @Override
    public void onMessage(Request request) {
        switch (request.getCommand()) {
            case Heartbeat:
                break;
            case Handshake:
                break;
            case Login:
                LOGIN_HANDLER.handle(request);
                break;
            case Bind:
                BIND_HANDLER.handle(request);
                break;
            case Kick:
                break;
            case Unknown:
                break;
        }
    }
}
