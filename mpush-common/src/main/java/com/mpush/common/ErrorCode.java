package com.mpush.common;

/**
 * Created by ohun on 2015/12/30.
 */
public enum ErrorCode {
    OFFLINE(1, "user offline"),
    PUSH_CLIENT_FAILURE(2, "push to client failure"),
    ROUTER_CHANGE(3, "router change"),
    DISPATCH_ERROR(100, "handle message error"),
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
