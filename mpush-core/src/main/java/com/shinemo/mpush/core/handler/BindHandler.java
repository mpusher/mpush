package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.gateway.router.RouterCenter;

/**
 * Created by ohun on 2015/12/23.
 */
public class BindHandler extends BaseMessageHandler<String> {
    @Override
    public String decodeBody(byte[] body) {
        return new String(body, Constants.UTF_8);
    }

    @Override
    public void handle(String body, Request request) {
        long userId = Long.parseLong(body);
        boolean success = RouterCenter.INSTANCE.publish(userId, request.getConnection());
        request.getResponse().send(new byte[]{success ? (byte) 1 : (byte) 0});
    }
}
