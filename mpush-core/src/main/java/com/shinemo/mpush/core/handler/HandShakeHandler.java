package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.SessionInfo;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.message.HandshakeMessage;
import com.shinemo.mpush.core.message.HandshakeSuccessMsg;
import com.shinemo.mpush.core.security.CipherManager;
import com.shinemo.mpush.core.security.ReusableSession;
import com.shinemo.mpush.core.security.ReusableSessionManager;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.crypto.AESUtils;
import com.shinemo.mpush.tools.crypto.RSAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.RSAPrivateKey;

/**
 * Created by ohun on 2015/12/24.
 */
public class HandshakeHandler extends BaseMessageHandler<HandshakeMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(HandshakeHandler.class);

    @Override
    public HandshakeMessage decodeBody(byte[] body) {
        RSAPrivateKey privateKey = CipherManager.INSTANCE.getPrivateKey();
        byte[] rawData = RSAUtils.decryptByPrivateKey(body, privateKey);
        return Jsons.fromJson(new String(rawData, Constants.UTF_8), HandshakeMessage.class);
    }

    @Override
    public void handle(HandshakeMessage body, Request request) {
        byte[] iv = body.iv;
        byte[] clientKey = body.clientKey;
        byte[] serverKey = CipherManager.INSTANCE.randomAESKey();
        byte[] sessionKey = CipherManager.INSTANCE.mixKey(clientKey, serverKey);//会话密钥混淆 Client random
        SessionInfo info = new SessionInfo(body.osName, body.osVersion, body.clientVersion,
                body.deviceId, sessionKey, iv);
        request.getConnection().setSessionInfo(info);
        ReusableSession session = ReusableSessionManager.INSTANCE.genSession(info);
        ReusableSessionManager.INSTANCE.saveSession(session);
        HandshakeSuccessMsg resp = new HandshakeSuccessMsg();
        resp.serverKey = serverKey;
        resp.serverHost = MPushUtil.getLocalIp();
        resp.serverTime = System.currentTimeMillis();
        resp.heartbeat = Constants.HEARTBEAT_TIME;
        resp.sessionId = session.sessionId;
        resp.expireTime = session.expireTime;
        byte[] responseData = AESUtils.encrypt(Jsons.toJson(resp).getBytes(Constants.UTF_8), clientKey, iv);
        request.getResponse().sendRaw(responseData);
        LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", sessionKey, clientKey, serverKey);

    }
}
