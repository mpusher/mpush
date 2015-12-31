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
    FAST_CONNECT(7),
    ERROR(8),
    OK(9),
    API(10),
    KICK(11),
    GATEWAY_KICK(12),
    PUSH(13),
    GATEWAY_PUSH(14),
    NOTIFICATION(15),
    GATEWAY_NOTIFICATION(16),
    CHAT(17),
    GATEWAY_CHAT(18),
    GROUP(19),
    GATEWAY_GROUP(20),
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
