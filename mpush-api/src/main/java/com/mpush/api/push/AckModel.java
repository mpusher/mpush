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

package com.mpush.api.push;

import com.mpush.api.protocol.Packet;

/**
 * Created by ohun on 16/9/6.
 *
 * @author ohun@live.cn (夜色)
 */
public enum AckModel {
    NO_ACK((byte) 0),//不需要ACK
    AUTO_ACK(Packet.FLAG_AUTO_ACK),//客户端收到消息后自动确认消息
    BIZ_ACK(Packet.FLAG_BIZ_ACK);//由客户端业务自己确认消息是否到达
    public final byte flag;

    AckModel(byte flag) {
        this.flag = flag;
    }
}
