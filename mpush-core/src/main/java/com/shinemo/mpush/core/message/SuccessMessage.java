package com.shinemo.mpush.core.message;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/28.
 */
public final class SuccessMessage extends BaseMessage {
    public String data;

    public SuccessMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(byte[] body) {
        if (body != null && body.length > 0) {
            data = new String(body, Constants.UTF_8);
        }
    }

    @Override
    public byte[] encode() {
        return Strings.isNullOrEmpty(data)
                ? null :
                data.getBytes(Constants.UTF_8);
    }

    public static SuccessMessage from(BaseMessage message) {
        return new SuccessMessage(message.createResponse(), message.connection);
    }

    public SuccessMessage setData(String data) {
        this.data = data;
        return this;
    }
}
