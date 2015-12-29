package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Created by ohun on 2015/12/28.
 */
public final class ErrorMessage extends ByteBufMessage {
    public String reason;
    public byte errorCode;

    public ErrorMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    public static ErrorMessage from(BaseMessage src) {
        return new ErrorMessage(new Packet(Command.ERROR.cmd, src.message.sessionId), src.connection);
    }

    public ErrorMessage setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public ErrorMessage setErrorCode(byte errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    @Override
    public void decode(ByteBuf body) {
        reason = decodeString(body);
        errorCode = decodeByte(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, reason);
        encodeByte(body, errorCode);
    }

    @Override
    public void send() {
        super.sendRaw();
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "reason='" + reason + '\'' +
                ", errorCode=" + errorCode +
                ", message=" + message +
                '}';
    }
}
