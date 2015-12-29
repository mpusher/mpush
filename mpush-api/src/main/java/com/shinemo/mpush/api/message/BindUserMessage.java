package com.shinemo.mpush.api.message;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/28.
 */
public final class BindUserMessage extends BaseMessage {
    public String userId;

    public BindUserMessage(Connection connection) {
        super(new Packet(Command.BIND.cmd, genSessionId()), connection);
    }

    public BindUserMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(byte[] body) {
        if (body != null && body.length > 0) {
            userId = new String(body, Constants.UTF_8);
        }
    }

    @Override
    public byte[] encode() {
        return Strings.isNullOrEmpty(userId)
                ? null :
                userId.getBytes(Constants.UTF_8);
    }
}
