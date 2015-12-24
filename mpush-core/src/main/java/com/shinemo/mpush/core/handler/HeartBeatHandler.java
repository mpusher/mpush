package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.ConnectionManager;
import com.shinemo.mpush.core.message.HeartBeatMessage;
import com.shinemo.mpush.core.message.LoginMessage;

/**
 * Created by ohun on 2015/12/22.
 */
public class HeartBeatHandler extends BaseMessageHandler<HeartBeatMessage> {
	
    @Override
    public HeartBeatMessage decodeBody(Packet packet) {
        return null;
    }

    @Override
    public void handle(HeartBeatMessage message, Request request) {
        request.getResponse().send(new byte[]{});
    }
    
}
