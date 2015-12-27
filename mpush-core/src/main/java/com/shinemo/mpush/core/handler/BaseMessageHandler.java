package com.shinemo.mpush.core.handler;


import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.Request;

/**
 * Created by ohun on 2015/12/22.
 */
public abstract class BaseMessageHandler<T> implements MessageHandler {
    @Override
    public void handle(Request request) {
        T t = decodeBody(request.getBody());
        handle(t, request);
    }

    public abstract T decodeBody(byte[] data);

    public abstract void handle(T body, Request request);
}
