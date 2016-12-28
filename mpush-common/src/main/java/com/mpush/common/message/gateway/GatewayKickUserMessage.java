/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.common.message.gateway;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.memory.PacketFactory;
import com.mpush.common.message.ByteBufMessage;
import com.mpush.common.router.KickRemoteMsg;
import io.netty.buffer.ByteBuf;

import static com.mpush.api.protocol.Command.GATEWAY_KICK;

/**
 * Created by ohun on 16/10/23.
 *
 * @author ohun@live.cn (夜色)
 */
public final class GatewayKickUserMessage extends ByteBufMessage implements KickRemoteMsg {
    public String userId;
    public String deviceId;
    public String connId;
    public int clientType;
    public String targetServer;
    public int targetPort;


    public GatewayKickUserMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    public static GatewayKickUserMessage build(Connection connection) {
        Packet packet = PacketFactory.get(GATEWAY_KICK);
        packet.sessionId = genSessionId();
        return new GatewayKickUserMessage(packet, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        userId = decodeString(body);
        deviceId = decodeString(body);
        connId = decodeString(body);
        clientType = decodeInt(body);
        targetServer = decodeString(body);
        targetPort = decodeInt(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, userId);
        encodeString(body, deviceId);
        encodeString(body, connId);
        encodeInt(body, clientType);
        encodeString(body, targetServer);
        encodeInt(body, targetPort);
    }

    public GatewayKickUserMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public GatewayKickUserMessage setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public GatewayKickUserMessage setConnId(String connId) {
        this.connId = connId;
        return this;
    }

    public GatewayKickUserMessage setClientType(int clientType) {
        this.clientType = clientType;
        return this;
    }

    public GatewayKickUserMessage setTargetServer(String targetServer) {
        this.targetServer = targetServer;
        return this;
    }

    public GatewayKickUserMessage setTargetPort(int targetPort) {
        this.targetPort = targetPort;
        return this;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getConnId() {
        return connId;
    }

    @Override
    public int getClientType() {
        return clientType;
    }

    @Override
    public String getTargetServer() {
        return targetServer;
    }

    @Override
    public int getTargetPort() {
        return targetPort;
    }

    @Override
    public String toString() {
        return "GatewayKickUserMessage{" +
                "userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", connId='" + connId + '\'' +
                ", clientType=" + clientType +
                ", targetServer='" + targetServer + '\'' +
                ", targetPort=" + targetPort +
                '}';
    }
}
