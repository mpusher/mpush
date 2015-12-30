package com.shinemo.mpush.api.protocol;

/**
 * Created by ohun on 2015/12/22.
 */
public enum Command {
    HEARTBEAT(1),
    HANDSHAKE(2),
    LOGIN(3),
    LOGOUT(4),
    BIND(5),
    UNBIND(6),
    KICK(7),
    FAST_CONNECT(8),
    ERROR(9),
    OK(10),
    PUSH(11),
    API(12),
    GATEWAY_PUSH(13),
    UNKNOWN(-1);

    Command(int cmd) {
        this.cmd = (byte) cmd;
    }

    public final byte cmd;

    public static Command toCMD(byte b) {
        if (b > 0 && b < values().length) return values()[b - 1];
        return UNKNOWN;
    }
}
