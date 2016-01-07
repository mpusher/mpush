package com.shinemo.mpush.core.handler;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.handler.BaseMessageHandler;
import com.shinemo.mpush.common.message.BindUserMessage;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.common.message.OkMessage;
import com.shinemo.mpush.core.router.RouterCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 */
public final class BindUserHandler extends BaseMessageHandler<BindUserMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(BindUserHandler.class);

    @Override
    public BindUserMessage decode(Packet packet, Connection connection) {
        return new BindUserMessage(packet, connection);
    }

    @Override
    public void handle(BindUserMessage message) {
        if (Strings.isNullOrEmpty(message.userId)) {
            ErrorMessage.from(message).setReason("invalid param").close();
            LOGGER.error("bind user failure invalid param, session={}", message.getConnection().getSessionContext());
            return;
        }
        SessionContext context = message.getConnection().getSessionContext();
        if (context.handshakeOk()) {
            boolean success = RouterCenter.INSTANCE.register(message.userId, message.getConnection());
            if (success) {
                OkMessage.from(message).setData("bind success").send();
                LOGGER.warn("bind user success, userId={}, session={}", message.userId, context);
            } else {
                ErrorMessage.from(message).setReason("bind failed").close();
                RouterCenter.INSTANCE.unRegister(message.userId);
                LOGGER.error("bind user failure, register router failure, userId={}, session={}", message.userId, context);
            }
        } else {
            ErrorMessage.from(message).setReason("not handshake").close();
            LOGGER.error("bind user failure not handshake, userId={}, session={}", message.userId, context);
        }
    }
}
