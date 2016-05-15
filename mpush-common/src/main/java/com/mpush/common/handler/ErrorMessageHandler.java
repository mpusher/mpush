package com.mpush.common.handler;

import com.mpush.api.connection.Connection;
import com.mpush.common.message.ErrorMessage;
import com.mpush.api.protocol.Packet;

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
