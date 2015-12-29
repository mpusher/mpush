package com.shinemo.mpush.core.handler;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.SessionContext;
import com.shinemo.mpush.api.message.BindUserMessage;
import com.shinemo.mpush.api.message.ErrorMessage;
import com.shinemo.mpush.api.message.OkMessage;
import com.shinemo.mpush.gateway.router.RouterCenter;

/**
 * Created by ohun on 2015/12/23.
 */
public final class BindHandler implements MessageHandler<BindUserMessage> {

    @Override
    public void handle(BindUserMessage message) {
        if (Strings.isNullOrEmpty(message.userId)) {
            ErrorMessage.from(message).setReason("invalid param").send();
            return;
        }
        SessionContext context = message.getConnection().getSessionContext();
        if (context.handshakeOk()) {
            boolean success = RouterCenter.INSTANCE.register(message.userId, message.getConnection());
            if (success) {
                OkMessage.from(message).setData("bind success").send();
                //TODO kick user
            } else {
                ErrorMessage.from(message).setReason("bind failed").send();
            }
        } else {
            ErrorMessage.from(message).setReason("not handshake").send();
        }
    }
}
