package com.shinemo.mpush.core.handler;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.SessionContext;
import com.shinemo.mpush.core.message.ErrorMessage;
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
public final class HandShakeHandler implements MessageHandler<HandShakeMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(HandShakeHandler.class);

    @Override
    public void handle(HandShakeMessage message) {
        byte[] iv = message.iv;//AES密钥向量16
        byte[] clientKey = message.clientKey;//客户端随机数
        byte[] serverKey = CipherBox.INSTANCE.randomAESKey();//服务端随机数
        byte[] sessionKey = CipherBox.INSTANCE.mixKey(clientKey, serverKey);//会话密钥

        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)
                || iv.length != CipherBox.AES_KEY_LENGTH
                || clientKey.length != CipherBox.AES_KEY_LENGTH) {
            ErrorMessage.from(message).setReason("Param invalid").send();
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            ErrorMessage.from(message).setReason("Repeat handshake").send();
            return;
        }

        //3.更换会话密钥RSA=>AES(clientKey)
        context.changeCipher(new AesCipher(clientKey, iv));

        //4.生成可复用session, 用于快速重连
        ReusableSession session = ReusableSessionManager.INSTANCE.genSession(context);
        ReusableSessionManager.INSTANCE.saveSession(session);

        //5.响应握手成功消息
        HandshakeSuccessMessage
                .from(message)
                .setServerKey(serverKey)
                .setServerHost(MPushUtil.getLocalIp())
                .setServerTime(System.currentTimeMillis())
                .setHeartbeat(Constants.HEARTBEAT_TIME)
                .setSessionId(session.sessionId)
                .setExpireTime(session.expireTime)
                .send();

        //6.更换会话密钥AES(clientKey)=>AES(sessionKey)
        context.changeCipher(new AesCipher(sessionKey, iv));

        //7.保存client信息
        context.setOsName(message.osName)
                .setOsVersion(message.osVersion)
                .setClientVersion(message.clientVersion)
                .setDeviceId(message.deviceId);

        LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", sessionKey, clientKey, serverKey);
    }
}