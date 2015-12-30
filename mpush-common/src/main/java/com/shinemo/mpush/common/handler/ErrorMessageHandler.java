package com.shinemo.mpush.common.handler;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/30.
 */
public class ErrorMessageHandler extends BaseMessageHandler<ErrorMessage> {
    @Override
    public ErrorMessage decode(Packet packet, Connection connection) {
        return new ErrorMessage(packet, connection);
    }

    @Override
    public void handle(ErrorMessage message) {

    }
}
