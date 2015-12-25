package com.shinemo.mpush.connection.netty.server;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.connection.netty.NettySharedHolder;
import com.shinemo.mpush.connection.netty.codec.PacketDecoder;
import com.shinemo.mpush.connection.netty.codec.PacketEncoder;
import com.shinemo.mpush.connection.netty.handler.ConnectionHandler;
import com.shinemo.mpush.core.MessageReceiver;
import com.shinemo.mpush.core.thread.ThreadPoolUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by ohun on 2015/12/22.
 */
public class ConnectionServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(ConnectionServer.class);

    private final AtomicBoolean startFlag = new AtomicBoolean(false);
    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public ConnectionServer(int port) {
        this.port = port;
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isRunning() {
        return startFlag.get();
    }

    @Override
    public void stop() {
        log.info("netty server stop now");
        this.startFlag.set(false);
        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();
    }

    @Override
    public void start() {
        if (!startFlag.compareAndSet(false, true)) {
            return;
        }

        /***
         * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
         * Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议。
         * 在这个例子中我们实现了一个服务端的应用，
         * 因此会有2个NioEventLoopGroup会被使用。
         * 第一个经常被叫做‘boss’，用来接收进来的连接。
         * 第二个经常被叫做‘worker’，用来处理已经被接收的连接，
         * 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
         * 如何知道多少个线程已经被使用，如何映射到已经创建的Channels上都需要依赖于EventLoopGroup的实现，
         * 并且可以通过构造函数来配置他们的关系。
         */
        this.bossGroup = new NioEventLoopGroup(0, ThreadPoolUtil.getBossExecutor());
        this.workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), ThreadPoolUtil.getWorkExecutor());

        try {

            /**
             * ServerBootstrap 是一个启动NIO服务的辅助启动类
             * 你可以在这个服务中直接使用Channel
             */

            ServerBootstrap b = new ServerBootstrap();

            /**
             * 这一步是必须的，如果没有设置group将会报java.lang.IllegalStateException: group not set异常
             */
            b.group(bossGroup, workerGroup);

            /***
             * ServerSocketChannel以NIO的selector为基础进行实现的，用来接收新的连接
             * 这里告诉Channel如何获取新的连接.
             */
            b.channel(NioServerSocketChannel.class);

            final ConnectionHandler connectionHandler = new ConnectionHandler(new MessageReceiver());

            /***
             * 这里的事件处理类经常会被用来处理一个最近的已经接收的Channel。
             * ChannelInitializer是一个特殊的处理类，
             * 他的目的是帮助使用者配置一个新的Channel。
             * 也许你想通过增加一些处理类比如NettyServerHandler来配置一个新的Channel
             * 或者其对应的ChannelPipeline来实现你的网络程序。
             * 当你的程序变的复杂时，可能你会增加更多的处理类到pipeline上，
             * 然后提取这些匿名类到最顶层的类上。
             */
            b.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new PacketDecoder());
                    ch.pipeline().addLast(PacketEncoder.INSTANCE);
                    ch.pipeline().addLast(connectionHandler);
                }
            });

            /***
             * 你可以设置这里指定的通道实现的配置参数。
             * 我们正在写一个TCP/IP的服务端，
             * 因此我们被允许设置socket的参数选项比如tcpNoDelay和keepAlive。
             * 请参考ChannelOption和详细的ChannelConfig实现的接口文档以此可以对ChannelOptions的有一个大概的认识。
             */
            b.option(ChannelOption.SO_BACKLOG, 1024);

            /***
             * option()是提供给NioServerSocketChannel用来接收进来的连接。
             * childOption()是提供给由父管道ServerChannel接收到的连接，
             * 在这个例子中也是NioServerSocketChannel。
             */
            b.childOption(ChannelOption.SO_KEEPALIVE, true);

            b.option(ChannelOption.ALLOCATOR, NettySharedHolder.byteBufAllocator);
            b.childOption(ChannelOption.ALLOCATOR, NettySharedHolder.byteBufAllocator);


            /***
             * 绑定端口并启动去接收进来的连接
             */
            ChannelFuture f = b.bind(port).sync();

            log.info("server start ok on:" + port);


            /**
             * 这里会一直等待，直到socket被关闭
             */
            f.channel().closeFuture().sync();

            log.info("server start ok on:" + port);

        } catch (Exception e) {
            log.error("server start exception", e);
            /***
             * 优雅关闭
             */
            stop();
        }
    }
}
