package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.Response;
import com.shinemo.mpush.api.SessionInfo;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.tools.IOUtils;
import com.shinemo.mpush.tools.crypto.AESUtils;

/**
 * Created by ohun on 2015/12/22.
 */
public class NettyResponse implements Response {
    private final Packet packet;
    private final Connection connection;

    public NettyResponse(Packet packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
    }

    public void send(byte[] body) {
        byte[] tmp = body;
        //1.压缩
        if (tmp.length > Constants.COMPRESS_LIMIT) {
            byte[] result = IOUtils.compress(tmp);
            if (result.length > 0) {
                tmp = result;
                packet.setFlag(Constants.COMPRESS_FLAG);
            }
        }
        //2.加密
        SessionInfo info = connection.getSessionInfo();
        if (info != null && info.sessionKey != null) {
            tmp = AESUtils.encrypt(tmp, info.sessionKey, info.iv);
            packet.setFlag(Constants.CRYPTO_FLAG);
        }
        packet.body = tmp;
        connection.send(packet);
    }

    public void sendRaw(byte[] body) {
        packet.body = body;
        connection.send(packet);
    }


    public void sendError(byte[] reason) {
        packet.body = reason;
        connection.send(packet);
    }

    @Override
    public void send(String body) {
        send(body.getBytes(Constants.UTF_8));
    }

    @Override
    public void sendRaw(String body) {
        sendRaw(body.getBytes(Constants.UTF_8));
    }

    @Override
    public void sendError(String reason) {
        sendError(reason.getBytes(Constants.UTF_8));
    }
}
