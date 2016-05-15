package com.mpush.common.handler;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.message.ErrorMessage;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
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
