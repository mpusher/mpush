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

package com.mpush.common.message.gateway;

import com.alibaba.fastjson.TypeReference;
import com.mpush.api.common.Condition;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.api.spi.push.IPushMessage;
import com.mpush.common.condition.*;
import com.mpush.common.memory.PacketFactory;
import com.mpush.common.message.ByteBufMessage;
import com.mpush.tools.Jsons;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;

import java.util.Set;

import static com.mpush.api.protocol.Command.GATEWAY_PUSH;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public final class GatewayPushMessage extends ByteBufMessage implements IPushMessage {
    public String userId;
    public int clientType;
    public int timeout;
    public byte[] content;

    public String taskId;
    public Set<String> tags;
    public String condition;

    public GatewayPushMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    public static GatewayPushMessage build(Connection connection) {
        Packet packet = PacketFactory.get(GATEWAY_PUSH);
        packet.sessionId = genSessionId();
        return new GatewayPushMessage(packet, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        userId = decodeString(body);
        clientType = decodeInt(body);
        timeout = decodeInt(body);
        content = decodeBytes(body);
        taskId = decodeString(body);
        tags = decodeSet(body);
        condition = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, userId);
        encodeInt(body, clientType);
        encodeInt(body, timeout);
        encodeBytes(body, content);
        encodeString(body, taskId);
        encodeSet(body, tags);
        encodeString(body, condition);
    }

    private Set<String> decodeSet(ByteBuf body) {
        String json = decodeString(body);
        if (json == null) return null;
        return Jsons.fromJson(json, new TypeReference<Set<String>>() {
        }.getType());
    }

    private void encodeSet(ByteBuf body, Set<String> field) {
        String json = field == null ? null : Jsons.toJson(field);
        encodeString(body, json);
    }

    public GatewayPushMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public GatewayPushMessage setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public GatewayPushMessage setClientType(int clientType) {
        this.clientType = clientType;
        return this;
    }

    public GatewayPushMessage addFlag(byte flag) {
        packet.addFlag(flag);
        return this;
    }

    public GatewayPushMessage setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public GatewayPushMessage setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public GatewayPushMessage setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public GatewayPushMessage setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    @Override
    public boolean isBroadcast() {
        return userId == null;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public int getClientType() {
        return clientType;
    }

    @Override
    public int getTimeoutMills() {
        return timeout;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public boolean isNeedAck() {
        return packet.hasFlag(Packet.FLAG_BIZ_ACK) || packet.hasFlag(Packet.FLAG_AUTO_ACK);
    }

    @Override
    public byte getFlags() {
        return packet.flags;
    }

    @Override
    public Condition getCondition() {
        if (condition != null) {
            return new ScriptCondition(condition);
        }
        if (tags != null) {
            return new TagsCondition(tags);
        }
        return AwaysPassCondition.I;
    }


    @Override
    public void finalized() {
        this.content = null;
        this.condition = null;
        this.tags = null;
    }

    @Override
    public void send() {
        super.sendRaw();
    }

    @Override
    public void send(ChannelFutureListener listener) {
        super.sendRaw(listener);
    }

    @Override
    public String toString() {
        return "GatewayPushMessage{" +
                "userId='" + userId + '\'' +
                ", clientType='" + clientType + '\'' +
                ", timeout='" + timeout + '\'' +
                ", content='" + (content == null ? 0 : content.length) + '\'' +
                '}';
    }
}
