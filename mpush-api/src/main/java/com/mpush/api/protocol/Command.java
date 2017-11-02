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

package com.mpush.api.protocol;

/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn
 */
public enum Command {
    HEARTBEAT(1),
    HANDSHAKE(2),
    LOGIN(3),
    LOGOUT(4),
    BIND(5),
    UNBIND(6),
    FAST_CONNECT(7),
    PAUSE(8),
    RESUME(9),
    ERROR(10),
    OK(11),
    HTTP_PROXY(12),
    KICK(13),
    GATEWAY_KICK(14),
    PUSH(15),
    GATEWAY_PUSH(16),
    NOTIFICATION(17),
    GATEWAY_NOTIFICATION(18),
    CHAT(19),
    GATEWAY_CHAT(20),
    GROUP(21),
    GATEWAY_GROUP(22),
    ACK(23),
    NACK(24),
    UNKNOWN(-1);

    Command(int cmd) {
        this.cmd = (byte) cmd;
    }

    public final byte cmd;

    public static Command toCMD(byte b) {
        Command[] values = values();
        if (b > 0 && b < values.length) return values[b - 1];
        return UNKNOWN;
    }
}
