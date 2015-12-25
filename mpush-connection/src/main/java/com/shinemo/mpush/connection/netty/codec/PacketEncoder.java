package com.shinemo.mpush.connection.netty.codec;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by ohun on 2015/12/19.
 */
@ChannelHandler.Sharable
public class PacketEncoder extends MessageToByteEncoder<Packet> {
    public static final PacketEncoder INSTANCE = new PacketEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        out.writeByte(Constants.MAGIC_NUM1);
        out.writeByte(Constants.MAGIC_NUM2);
        out.writeInt(packet.getBodyLength());
        out.writeByte(packet.command);
        out.writeByte(packet.flags);
        out.writeByte(packet.version);
        out.writeInt(packet.msgId);
        if (packet.getBodyLength() > 0) {
            out.writeBytes(packet.body);
        }
    }
}
