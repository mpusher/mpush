package com.shinemo.mpush.core.handler;


import com.shinemo.mpush.api.Message;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public abstract class BaseMessageHandler<T extends Message> implements MessageHandler {
    @Override
    public void handle(Request request) {

    }

    public abstract T decodeBody(Packet packet);

    public abstract void handle(T t, Request request);
}
