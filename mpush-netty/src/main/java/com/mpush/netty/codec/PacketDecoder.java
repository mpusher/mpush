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
import com.mpush.tools.config.CC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by ohun on 2015/12/19.
 * length(4)+cmd(1)+cc(2)+flags(1)+sessionId(4)+lrc(1)+body(n)
 *
 * @author ohun@live.cn
 */
public final class PacketDecoder extends ByteToMessageDecoder {
    private static final int maxPacketSize = CC.mp.core.max_packet_size;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        decodeHeartbeat(in, out);
        decodeFrames(in, out);
    }

    private void decodeHeartbeat(ByteBuf in, List<Object> out) {
        while (in.isReadable()) {
            if (in.readByte() == Packet.HB_PACKET_BYTE) {
                out.add(Packet.HB_PACKE);
            } else {
                in.readerIndex(in.readerIndex() - 1);
                break;
            }
        }
    }

    private void decodeFrames(ByteBuf in, List<Object> out) throws Exception {
        try {
            while (in.readableBytes() >= Packet.HEADER_LEN) {
                //1.记录当前读取位置位置.如果读取到非完整的frame,要恢复到该位置,便于下次读取
                in.markReaderIndex();
                out.add(decodeFrame(in));
            }
        } catch (DecodeException e) {
            //2.读取到不完整的frame,恢复到最近一次正常读取的位置,便于下次读取
            in.resetReaderIndex();
        }
    }

    private Packet decodeFrame(ByteBuf in) throws Exception {
        int bufferSize = in.readableBytes();
        int bodyLength = in.readInt();
        if (bufferSize < (bodyLength + Packet.HEADER_LEN)) {
            throw new DecodeException("invalid frame");
        }
        return readPacket(in, bodyLength);
    }

    private Packet readPacket(ByteBuf in, int bodyLength) {
        byte command = in.readByte();
        short cc = in.readShort();
        byte flags = in.readByte();
        int sessionId = in.readInt();
        byte lrc = in.readByte();
        byte[] body = null;
        if (bodyLength > 0) {
            if (bodyLength > maxPacketSize) {
                throw new RuntimeException("ERROR PACKET_SIZE：" + bodyLength);
            }
            body = new byte[bodyLength];
            in.readBytes(body);
        }
        Packet packet = new Packet(command);
        packet.cc = cc;
        packet.flags = flags;
        packet.sessionId = sessionId;
        packet.lrc = lrc;
        packet.body = body;
        return packet;
    }
}
