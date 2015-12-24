package com.shinemo.mpush.connection;

import org.junit.Test;

import com.shinemo.mpush.connection.netty.encoder.PacketDecoder;
import com.shinemo.mpush.connection.netty.encoder.PacketEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by ohun on 2015/12/24.
 */
public class ClientTest {

	@Test
	public void testClient() {
        String host = "127.0.0.1";
        int port = 3000;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new PacketDecoder());
                    ch.pipeline().addLast(PacketEncoder.INSTANCE);
                    ch.pipeline().addLast(new ClientHandler());
                }
            });
            ChannelFuture f = b.connect(host, port).sync(); // (5)
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
