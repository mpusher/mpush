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
public final class UnbindUserHandler extends BaseMessageHandler<BindUserMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(UnbindUserHandler.class);

    @Override
    public BindUserMessage decode(Packet packet, Connection connection) {
        return new BindUserMessage(packet, connection);
    }

    @Override
    public void handle(BindUserMessage message) {
        if (Strings.isNullOrEmpty(message.userId)) {
            ErrorMessage.from(message).setReason("invalid param").close();
            LOGGER.error("unbind user failure invalid param, session={}", message.getConnection().getSessionContext());
            return;
        }
        //1.绑定用户时先看下是否握手成功
        SessionContext context = message.getConnection().getSessionContext();
        if (context.handshakeOk()) {
            //2.如果握手成功，就把用户链接信息注册到路由中心，本地和远程各一份
            boolean success = RouterCenter.INSTANCE.unRegister(message.userId);
            if (success) {
                OkMessage.from(message).setData("unbind success").send();
                LOGGER.warn("unbind user success, userId={}, session={}", message.userId, context);
            } else {
                ErrorMessage.from(message).setReason("unbind failed").close();
                LOGGER.error("unbind user failure, register router failure, userId={}, session={}", message.userId, context);
            }
        } else {
            ErrorMessage.from(message).setReason("not handshake").close();
            LOGGER.error("unbind user failure not handshake, userId={}, session={}", message.userId, context);
        }
    }
}
