package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Response {

    void send(byte[] body);

    void sendRaw(byte[] body);

    void sendError(byte[] reason);
}
