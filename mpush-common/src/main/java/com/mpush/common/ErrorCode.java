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

package com.mpush.common;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public enum ErrorCode {
    OFFLINE(1, "user offline"),
    PUSH_CLIENT_FAILURE(2, "push to client failure"),
    ROUTER_CHANGE(3, "router change"),
    ACK_TIMEOUT(4, "ack timeout"),
    DISPATCH_ERROR(100, "handle message error"),
    UNSUPPORTED_CMD(101, "unsupported command"),
    REPEAT_HANDSHAKE(102, "repeat handshake"),
    SESSION_EXPIRED(103, "session expired"),
    INVALID_DEVICE(104, "invalid device"),
    UNKNOWN(-1, "unknown");

    ErrorCode(int code, String errorMsg) {
        this.errorMsg = errorMsg;
        this.errorCode = (byte) code;
    }

    public final byte errorCode;
    public final String errorMsg;

    public static ErrorCode toEnum(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.errorCode == code) {
                return errorCode;
            }
        }
        return UNKNOWN;
    }
}
