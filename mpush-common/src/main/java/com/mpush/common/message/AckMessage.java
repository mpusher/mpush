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

package com.mpush.common.message;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Created by ohun on 16/9/5.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AckMessage extends BaseMessage {


    public AckMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    @Override
    public void decode(byte[] body) {

    }

    @Override
    public byte[] encode() {
        return null;
    }


    public static AckMessage from(BaseMessage src) {
        return new AckMessage(new Packet(Command.ACK, src.getSessionId()), src.connection);
    }

    @Override
    public String toString() {
        return "AckMessage{" +
                "packet=" + packet +
                '}';
    }
}
