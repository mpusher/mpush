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
    // 心跳
    HEARTBEAT(1),
    // 握手
    HANDSHAKE(2),
    // 登录
    LOGIN(3),
    // 登出
    LOGOUT(4),
    // 绑定
    BIND(5),
    // 解绑
    UNBIND(6),
    // 快速链接
    FAST_CONNECT(7),
    // 暂停
    PAUSE(8),
    // 恢复
    RESUME(9),
    // 错误
    ERROR(10),
    // 正常
    OK(11),
    // http代理
    HTTP_PROXY(12),
    // 踢除
    KICK(13),
    // 网关踢除
    GATEWAY_KICK(14),
    // 推送
    PUSH(15),
    // 网关推送
    GATEWAY_PUSH(16),
    // 通知
    NOTIFICATION(17),
    // 网关通知
    GATEWAY_NOTIFICATION(18),
    // 聊天
    CHAT(19),
    // 网关聊天
    GATEWAY_CHAT(20),
    // 组
    GROUP(21),
    // 网关组
    GATEWAY_GROUP(22),
    // 确认
    ACK(23),
    // 不确认
    NACK(24),
    // 未知
    UNKNOWN(-1);

    Command(int cmd) {
        this.cmd = (byte) cmd;
    }

    public final byte cmd;

    /**
     * 从数据包中获取命令
     * @param b
     * @return
     */
    public static Command toCMD(byte b) {
        Command[] values = values();
        if (b > 0 && b < values.length) return values[b - 1];
        return UNKNOWN;
    }
}
