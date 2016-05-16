package com.mpush.core.handler;

import com.google.common.base.Strings;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.HandshakeEvent;
import com.mpush.api.protocol.Packet;
import com.mpush.common.EventBus;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.HandshakeMessage;
import com.mpush.common.message.HandshakeOkMessage;
import com.mpush.common.security.AesCipher;
import com.mpush.common.security.CipherBox;
import com.mpush.core.session.ReusableSession;
import com.mpush.core.session.ReusableSessionManager;
import com.mpush.log.Logs;
import com.mpush.tools.MPushUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/24.
 *
 * @author ohun@live.cn
 */
public final class HandshakeHandler extends BaseMessageHandler<HandshakeMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(HandshakeHandler.class);

    @Override
    public HandshakeMessage decode(Packet packet, Connection connection) {
    	return new HandshakeMessage(packet, connection);
    }

    @Override
    public void handle(HandshakeMessage message) {
    	
        byte[] iv = message.iv;//AES密钥向量16位
        byte[] clientKey = message.clientKey;//客户端随机数16位
        byte[] serverKey = CipherBox.INSTANCE.randomAESKey();//服务端随机数16位
        byte[] sessionKey = CipherBox.INSTANCE.mixKey(clientKey, serverKey);//会话密钥16位

        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)
                || iv.length != CipherBox.INSTANCE.getAesKeyLength()
                || clientKey.length != CipherBox.INSTANCE.getAesKeyLength()) {
            ErrorMessage.from(message).setReason("Param invalid").close();
            Logs.Conn.info("client handshake false:{}", message.getConnection());
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            Logs.Conn.info("client handshake false for repeat handshake:{}", message.getConnection().getSessionContext());
            return;
        }

        //3.更换会话密钥RSA=>AES(clientKey)
        context.changeCipher(new AesCipher(clientKey, iv));

        //4.生成可复用session, 用于快速重连
        ReusableSession session = ReusableSessionManager.INSTANCE.genSession(context);

        //5.计算心跳时间
        int heartbeat = MPushUtil.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);

        //6.响应握手成功消息
        HandshakeOkMessage
                .from(message)
                .setServerKey(serverKey)
                .setHeartbeat(heartbeat)
                .setSessionId(session.sessionId)
                .setExpireTime(session.expireTime)
                .send();

        //7.更换会话密钥AES(clientKey)=>AES(sessionKey)
        context.changeCipher(new AesCipher(sessionKey, iv));

        //8.保存client信息到当前连接
        context.setOsName(message.osName)
                .setOsVersion(message.osVersion)
                .setClientVersion(message.clientVersion)
                .setDeviceId(message.deviceId)
                .setHeartbeat(heartbeat);

        //9.保存可复用session到Redis, 用于快速重连
        ReusableSessionManager.INSTANCE.cacheSession(session);

        //10.触发握手成功事件
        EventBus.INSTANCE.post(new HandshakeEvent(message.getConnection(), heartbeat));
        Logs.Conn.info("client handshake success:{}", context);
    }
}