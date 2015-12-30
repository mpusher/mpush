package com.shinemo.mpush.common.handler;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.common.message.OkMessage;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/30.
 */
public class OkMessageHandler extends BaseMessageHandler<OkMessage> {
    @Override
    public OkMessage decode(Packet packet, Connection connection) {
        return new OkMessage(packet, connection);
    }

    @Override
    public void handle(OkMessage message) {

    }
}
