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

package com.mpush.api.protocol;

import java.net.InetSocketAddress;

/**
 * Created by ohun on 16/10/21.
 *
 * @author ohun@live.cn (夜色)
 */
public final class UDPPacket extends Packet {
    private InetSocketAddress sender;

    public UDPPacket(byte cmd, InetSocketAddress sender) {
        super(cmd);
        this.sender = sender;
    }

    public UDPPacket(Command cmd, int sessionId, InetSocketAddress sender) {
        super(cmd, sessionId);
        this.sender = sender;
    }

    public UDPPacket(byte cmd) {
        super(cmd);
    }

    public UDPPacket(Command cmd) {
        super(cmd);
    }

    public UDPPacket(Command cmd, int sessionId) {
        super(cmd, sessionId);
    }

    @Override
    public InetSocketAddress sender() {
        return sender;
    }

    @Override
    public void sender(InetSocketAddress sender) {
        this.sender = sender;
    }

    @Override
    public Packet response(Command command) {
        return new UDPPacket(command, sessionId, sender);
    }
}
