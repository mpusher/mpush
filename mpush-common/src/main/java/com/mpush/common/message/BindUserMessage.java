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

package com.mpush.common.message;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Created by ohun on 2015/12/28.
 *
 * @author ohun@live.cn
 */
public final class BindUserMessage extends ByteBufMessage {
    public String userId;
    public String tags;
    public String data;

    public BindUserMessage(Connection connection) {
        super(new Packet(Command.BIND, genSessionId()), connection);
    }

    public BindUserMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        userId = decodeString(body);
        data = decodeString(body);
        tags = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, userId);
        encodeString(body, data);
        encodeString(body, tags);
    }

    @Override
    public void decodeJsonBody(Map<String, Object> body) {
        userId = (String) body.get("userId");
        tags = (String) body.get("tags");
        data = (String) body.get("data");
    }

    @Override
    public String toString() {
        return "BindUserMessage{" +
                "data='" + data + '\'' +
                ", userId='" + userId + '\'' +
                ", tags='" + tags + '\'' +
                ", packet=" + packet +
                '}';
    }
}
