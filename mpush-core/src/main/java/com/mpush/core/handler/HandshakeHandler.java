/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.core.handler;

import com.google.common.base.Strings;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.HandshakeEvent;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.HandshakeMessage;
import com.mpush.common.message.HandshakeOkMessage;
import com.mpush.common.security.AesCipher;
import com.mpush.common.security.CipherBox;
import com.mpush.core.session.ReusableSession;
import com.mpush.core.session.ReusableSessionManager;
import com.mpush.tools.config.ConfigManager;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.log.Logs;

/**
 * Created by ohun on 2015/12/24.
 *
 * @author ohun@live.cn
 */
public final class HandshakeHandler extends BaseMessageHandler<HandshakeMessage> {

    @Override
    public HandshakeMessage decode(Packet packet, Connection connection) {
        return new HandshakeMessage(packet, connection);
    }

    @Override
    public void handle(HandshakeMessage message) {

        byte[] iv = message.iv;//AES密钥向量16位
        byte[] clientKey = message.clientKey;//客户端随机数16位
        byte[] serverKey = CipherBox.I.randomAESKey();//服务端随机数16位
        byte[] sessionKey = CipherBox.I.mixKey(clientKey, serverKey);//会话密钥16位

        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)
                || iv.length != CipherBox.I.getAesKeyLength()
                || clientKey.length != CipherBox.I.getAesKeyLength()) {
            ErrorMessage.from(message).setReason("Param invalid").close();
            Logs.Conn.info("handshake failure, message={}", message.toString());
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            Logs.Conn.info("handshake failure, repeat handshake, session={}", message.getConnection().getSessionContext());
            return;
        }

        //3.更换会话密钥RSA=>AES(clientKey)
        context.changeCipher(new AesCipher(clientKey, iv));

        //4.生成可复用session, 用于快速重连
        ReusableSession session = ReusableSessionManager.I.genSession(context);

        //5.计算心跳时间
        int heartbeat = ConfigManager.I.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);

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
        ReusableSessionManager.I.cacheSession(session);

        //10.触发握手成功事件
        EventBus.I.post(new HandshakeEvent(message.getConnection(), heartbeat));
        Logs.Conn.info(">>> handshake success, session={}", context);
    }
}