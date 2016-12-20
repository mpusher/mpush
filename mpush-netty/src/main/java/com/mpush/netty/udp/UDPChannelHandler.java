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

package com.mpush.netty.udp;

import com.mpush.api.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.netty.codec.PacketDecoder;
import com.mpush.netty.connection.NettyConnection;
import com.mpush.tools.log.Logs;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Created by ohun on 16/10/21.
 *
 * @author ohun@live.cn (夜色)
 */
@ChannelHandler.Sharable
public final class UDPChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(UDPChannelHandler.class);
    private final NettyConnection connection = new NettyConnection();
    private final PacketReceiver receiver;
    private InetAddress multicastAddress;
    private NetworkInterface networkInterface;

    public UDPChannelHandler(PacketReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connection.init(ctx.channel(), false);
        if (multicastAddress != null) {
            ((DatagramChannel) ctx.channel()).joinGroup(multicastAddress, networkInterface, null).addListener(future -> {
                if (future.isSuccess()) {
                    Logs.CONN.info("join multicast group success, channel={}, group={}", ctx.channel(), multicastAddress);
                } else {
                    LOGGER.error("join multicast group error, channel={}, group={}", ctx.channel(), multicastAddress, future.cause());
                }
            });
        }
        Logs.CONN.info("init udp channel={}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connection.close();
        if (multicastAddress != null) {
            ((DatagramChannel) ctx.channel()).leaveGroup(multicastAddress, networkInterface, null).addListener(future -> {
                if (future.isSuccess()) {
                    Logs.CONN.info("leave multicast group success, channel={}, group={}", ctx.channel(), multicastAddress);
                } else {
                    LOGGER.error("leave multicast group error, channel={}, group={}", ctx.channel(), multicastAddress, future.cause());
                }
            });
        }
        Logs.CONN.info("disconnect udp channel={}, connection={}", ctx.channel(), connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        Packet packet = PacketDecoder.decodeFrame(datagramPacket);
        receiver.onReceive(packet, connection);
        datagramPacket.release();//最后一个使用方要释放引用
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connection.close();
        Logs.CONN.error("udp channel caught an exception channel={}, connection={}", ctx.channel(), connection);
        LOGGER.error("caught an ex, channel={}, connection={}", ctx.channel(), connection, cause);
    }

    public UDPChannelHandler setMulticastAddress(InetAddress multicastAddress) {
        if (!multicastAddress.isMulticastAddress()) {
            throw new IllegalArgumentException(multicastAddress + "not a multicastAddress");
        }

        this.multicastAddress = multicastAddress;
        return this;
    }

    public UDPChannelHandler setNetworkInterface(NetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
        return this;
    }

    public Connection getConnection() {
        return connection;
    }
}
