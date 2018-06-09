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
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.HandshakeMessage;
import com.mpush.common.message.HandshakeOkMessage;
import com.mpush.common.security.AesCipher;
import com.mpush.common.security.CipherBox;
import com.mpush.core.MPushServer;
import com.mpush.core.session.ReusableSession;
import com.mpush.core.session.ReusableSessionManager;
import com.mpush.tools.config.ConfigTools;
import com.mpush.tools.log.Logs;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import static com.mpush.common.ErrorCode.REPEAT_HANDSHAKE;

/**
 * Created by ohun on 2015/12/24.
 *
 * @author ohun@live.cn
 */
public final class HandshakeHandler extends BaseMessageHandler<HandshakeMessage> {

    private final ReusableSessionManager reusableSessionManager;

    public HandshakeHandler(MPushServer mPushServer) {
        this.reusableSessionManager = mPushServer.getReusableSessionManager();
    }

    @Override
    public HandshakeMessage decode(Packet packet, Connection connection) {
        return new HandshakeMessage(packet, connection);
    }

    @Override
    public void handle(HandshakeMessage message) {
        if (message.getConnection().getSessionContext().isSecurity()) {
            doSecurity(message);
        } else {
            doInsecurity(message);
        }
    }

    private void doSecurity(HandshakeMessage message) {
        byte[] iv = message.iv;//AES密钥向量16位
        byte[] clientKey = message.clientKey;//客户端随机数16位
        byte[] serverKey = CipherBox.I.randomAESKey();//服务端随机数16位
        byte[] sessionKey = CipherBox.I.mixKey(clientKey, serverKey);//会话密钥16位

        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)
                || iv.length != CipherBox.I.getAesKeyLength()
                || clientKey.length != CipherBox.I.getAesKeyLength()) {
            ErrorMessage.from(message).setReason("Param invalid").close();
            Logs.CONN.error("handshake failure, message={}, conn={}", message, message.getConnection());
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            ErrorMessage.from(message).setErrorCode(REPEAT_HANDSHAKE).send();
            Logs.CONN.warn("handshake failure, repeat handshake, conn={}", message.getConnection());
            return;
        }

        //3.更换会话密钥RSA=>AES(clientKey)
        context.changeCipher(new AesCipher(clientKey, iv));

        //4.生成可复用session, 用于快速重连
        ReusableSession session = reusableSessionManager.genSession(context);

        //5.计算心跳时间
        int heartbeat = ConfigTools.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);

        //6.响应握手成功消息
        HandshakeOkMessage
                .from(message)
                .setServerKey(serverKey)
                .setHeartbeat(heartbeat)
                .setSessionId(session.sessionId)
                .setExpireTime(session.expireTime)
                .send(f -> {
                            if (f.isSuccess()) {
                                //7.更换会话密钥AES(clientKey)=>AES(sessionKey)
                                context.changeCipher(new AesCipher(sessionKey, iv));
                                //8.保存client信息到当前连接
                                context.setOsName(message.osName)
                                        .setOsVersion(message.osVersion)
                                        .setClientVersion(message.clientVersion)
                                        .setDeviceId(message.deviceId)
                                        .setHeartbeat(heartbeat);

                                //9.保存可复用session到Redis, 用于快速重连
                                reusableSessionManager.cacheSession(session);

                                Logs.CONN.info("handshake success, conn={}", message.getConnection());
                            } else {
                                Logs.CONN.info("handshake failure, conn={}", message.getConnection(), f.cause());
                            }
                        }
                );
    }

    private void doInsecurity(HandshakeMessage message) {

        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)) {
            ErrorMessage.from(message).setReason("Param invalid").close();
            Logs.CONN.error("handshake failure, message={}, conn={}", message, message.getConnection());
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            ErrorMessage.from(message).setErrorCode(REPEAT_HANDSHAKE).send();
            Logs.CONN.warn("handshake failure, repeat handshake, conn={}", message.getConnection());
            return;
        }

        //6.响应握手成功消息
        HandshakeOkMessage.from(message).send();

        //8.保存client信息到当前连接
        context.setOsName(message.osName)
                .setOsVersion(message.osVersion)
                .setClientVersion(message.clientVersion)
                .setDeviceId(message.deviceId)
                .setHeartbeat(Integer.MAX_VALUE);

        Logs.CONN.info("handshake success, conn={}", message.getConnection());

    }
}