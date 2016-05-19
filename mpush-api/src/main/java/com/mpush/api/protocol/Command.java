package com.mpush.api.protocol;

/**
 * 1: 心跳
 * 2：握手
 * 3：login
 * 4：logout
 * 5：Bind User
 * 6：Unbind User
 * 7：快速连接
 * 8：暂停客户端
 * 9：恢复客户端
 * 10：错误
 * 11：ok
 * 12：HTTP代理
 * 13：踢用户
 * 14：网关踢用户
 * 15：PUSH
 * 16：网关PUSH
 * 17：通知
 * 18：网关通知
 * 19：chat
 * 20：网关chat
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
