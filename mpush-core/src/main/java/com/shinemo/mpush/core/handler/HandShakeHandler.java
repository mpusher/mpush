package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.SessionContext;
import com.shinemo.mpush.core.message.HandShakeMessage;
import com.shinemo.mpush.core.message.HandshakeSuccessMessage;
import com.shinemo.mpush.core.security.AesCipher;
import com.shinemo.mpush.core.security.CipherBox;
import com.shinemo.mpush.core.security.ReusableSession;
import com.shinemo.mpush.core.security.ReusableSessionManager;
import com.shinemo.mpush.tools.MPushUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/24.
 */
public class HandShakeHandler extends BaseMessageHandler<HandShakeMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(HandShakeHandler.class);

    @Override
    public void handle(HandShakeMessage message) {
        byte[] iv = message.iv;
        byte[] clientKey = message.clientKey;
        byte[] serverKey = CipherBox.INSTANCE.randomAESKey();
        byte[] sessionKey = CipherBox.INSTANCE.mixKey(clientKey, serverKey);//会话密钥混淆 Client random
        SessionContext info = new SessionContext(message.osName, message.osVersion,
                message.clientVersion, message.deviceId, sessionKey, iv);
        info.changeCipher(new AesCipher(clientKey, iv));
        message.getConnection().setSessionInfo(info);
        ReusableSession session = ReusableSessionManager.INSTANCE.genSession(info);
        ReusableSessionManager.INSTANCE.saveSession(session);
        HandshakeSuccessMessage resp = message.createSuccessMessage();
        resp.serverKey = serverKey;
        resp.serverHost = MPushUtil.getLocalIp();
        resp.serverTime = System.currentTimeMillis();
        resp.heartbeat = Constants.HEARTBEAT_TIME;
        resp.sessionId = session.sessionId;
        resp.expireTime = session.expireTime;
        resp.send();
        info.changeCipher(new AesCipher(sessionKey, iv));
        LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", sessionKey, clientKey, serverKey);

    }
}