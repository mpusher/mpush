package com.mpush.common.message;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.ErrorCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;

import static com.mpush.api.protocol.Command.ERROR;

/**
 * Created by ohun on 2015/12/28.
 *
 * @author ohun@live.cn
 */
public final class ErrorMessage extends ByteBufMessage {
    public byte cmd;
    public byte code;
    public String reason;

    public ErrorMessage(byte cmd, Packet message, Connection connection) {
        super(message, connection);
        this.cmd = cmd;
    }

    public ErrorMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        cmd = decodeByte(body);
        code = decodeByte(body);
        reason = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeByte(body, cmd);
        encodeByte(body, code);
        encodeString(body, reason);
    }

    public static ErrorMessage from(BaseMessage src) {
        return new ErrorMessage(src.packet.cmd, new Packet(ERROR
                , src.packet.sessionId), src.connection);
    }

    public static ErrorMessage from(Packet src, Connection connection) {
        return new ErrorMessage(src.cmd, new Packet(ERROR, src.sessionId), connection);
    }


    public ErrorMessage setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public ErrorMessage setErrorCode(ErrorCode code) {
        this.code = code.errorCode;
        this.reason = code.errorMsg;
        return this;
    }

    @Override
    public void send() {
        super.sendRaw();
    }

    @Override
    public void close() {
        sendRaw(ChannelFutureListener.CLOSE);
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "reason='" + reason + '\'' +
                ", code=" + code +
                ", packet=" + packet +
                '}';
    }
}
