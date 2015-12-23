package com.shinemo.mpush.connection.netty.encoder;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.exception.DecodeException;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;

/**
 * Created by ohun on 2015/12/19.
 * magic(2)+length(4)+cmd(1)+version(1)+flags(1)+msgId(4)+body(n)
 */
public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        decodeFrames(in, out);
    }

    private void decodeFrames(ByteBuf in, List<Object> out) throws Exception {
        try {
            while (in.readableBytes() >= Constants.HEADER_LEN) {
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
        if (in.readByte() != Constants.MAGIC_NUM1 || in.readByte() != Constants.MAGIC_NUM2) {
            throw new RuntimeException("ERROR MAGIC_NUM");
        }
        int bodyLength = in.readInt();
        if (bufferSize < (bodyLength + Constants.HEADER_LEN)) {
            throw new DecodeException("invalid frame");
        }
        return readPacket(in, bodyLength);
    }

    private Packet readPacket(ByteBuf in, int bodyLength) {
        byte command = in.readByte();
        byte version = in.readByte();
        byte flags = in.readByte();
        int msgId = in.readInt();
        byte[] body = null;
        if (bodyLength > 0) {
            if (bodyLength > Constants.MAX_PACKET_SIZE) {
                throw new RuntimeException("ERROR PACKET_SIZE：" + bodyLength);
            }
            body = new byte[bodyLength];
            in.readBytes(body);
        }
        Packet packet = new Packet();
        packet.command = command;
        packet.version = version;
        packet.flags = flags;
        packet.msgId = msgId;
        packet.body = body;
        return packet;
    }

}
