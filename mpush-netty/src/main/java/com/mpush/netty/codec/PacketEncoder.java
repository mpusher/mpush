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

package com.mpush.netty.codec;

import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by ohun on 2015/12/19.
 * length(4)+cmd(1)+cc(2)+flags(1)+sessionId(4)+lrc(1)+body(n)
 *
 * @author ohun@live.cn
 */
@ChannelHandler.Sharable
public final class PacketEncoder extends MessageToByteEncoder<Packet> {
    public static final PacketEncoder INSTANCE = new PacketEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        encodeFrame(packet, out);
    }

    public static ByteBuf encode(Channel channel, Packet packet) {
        int capacity = packet.cmd == Command.HEARTBEAT.cmd ? 1 : Packet.HEADER_LEN + packet.getBodyLength();
        ByteBuf out = channel.alloc().buffer(capacity, capacity);
        encodeFrame(packet, out);
        return out;
    }

    public static void encodeFrame(Packet packet, ByteBuf out) {
        if (packet.cmd == Command.HEARTBEAT.cmd) {
            out.writeByte(Packet.HB_PACKET_BYTE);
        } else {
            out.writeInt(packet.getBodyLength());
            out.writeByte(packet.cmd);
            out.writeShort(packet.cc);
            out.writeByte(packet.flags);
            out.writeInt(packet.sessionId);
            out.writeByte(packet.lrc);
            if (packet.getBodyLength() > 0) {
                out.writeBytes(packet.body);
            }
        }
    }
}
