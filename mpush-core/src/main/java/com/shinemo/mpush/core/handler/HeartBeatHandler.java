package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public class HeartBeatHandler extends BaseMessageHandler<Void> {

    @Override
    public Void decodeBody(byte[] body) {
        return null;
    }

    @Override
    public void handle(Void message, Request request) {
        System.err.println("receive client heartbeat, time=" + System.currentTimeMillis());
    }

}
