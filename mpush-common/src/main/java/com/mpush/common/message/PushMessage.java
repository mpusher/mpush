package com.mpush.common.message;

import com.mpush.api.Constants;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;

import static com.mpush.api.protocol.Command.PUSH;

/**
 * Created by ohun on 2015/12/30.
 */
public final class PushMessage extends BaseMessage {

    public String content;

    public PushMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    public PushMessage(String content, Connection connection) {
        super(new Packet(PUSH, genSessionId()), connection);
        this.content = content;
    }

    @Override
    public void decode(byte[] body) {
        content = new String(body, Constants.UTF_8);
    }

    @Override
    public byte[] encode() {
        return content == null ? null : content.getBytes(Constants.UTF_8);
    }
}
