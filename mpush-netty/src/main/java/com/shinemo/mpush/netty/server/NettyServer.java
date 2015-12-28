package com.shinemo.mpush.netty.server;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.PooledByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.api.protocol.Handler;
import com.shinemo.mpush.netty.codec.PacketDecoder;
import com.shinemo.mpush.netty.codec.PacketEncoder;
import com.shinemo.mpush.netty.util.NettySharedHandler;
import com.shinemo.mpush.netty.util.NettySharedHolder;
import com.shinemo.mpush.tools.thread.ThreadPoolUtil;

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
public class NettyServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private final AtomicBoolean startFlag = new AtomicBoolean(false);
    private final int port;
    private final Handler channelHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(int port,Handler channelHandler) {
        this.port = port;
        this.channelHandler = channelHandler;
    }
    
    public NettyServer(int port) {
    	this(port,null);
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
            
            final NettySharedHandler nettySharedHandler = new NettySharedHandler(channelHandler);

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
                    ch.pipeline().addLast(nettySharedHandler);
                }
            });

            /***
             * 你可以设置这里指定的通道实现的配置参数。
             * 我们正在写一个TCP/IP的服务端，
             * 因此我们被允许设置socket的参数选项比如tcpNoDelay和keepAlive。
             * 请参考ChannelOption和详细的ChannelConfig实现的接口文档以此可以对ChannelOptions的有一个大概的认识。
             */
            b.option(ChannelOption.SO_BACKLOG, 1024);

            /**
             * TCP层面的接收和发送缓冲区大小设置，
             * 在Netty中分别对应ChannelOption的SO_SNDBUF和SO_RCVBUF，
             * 需要根据推送消息的大小，合理设置，对于海量长连接，通常32K是个不错的选择。
             */
            b.childOption(ChannelOption.SO_SNDBUF, 32 * 1024);
            b.childOption(ChannelOption.SO_RCVBUF, 32 * 1024);

            /***
             * option()是提供给NioServerSocketChannel用来接收进来的连接。
             * childOption()是提供给由父管道ServerChannel接收到的连接，
             * 在这个例子中也是NioServerSocketChannel。
             */
            b.childOption(ChannelOption.SO_KEEPALIVE, true);


            /**
             * 在Netty 4中实现了一个新的ByteBuf内存池，它是一个纯Java版本的 jemalloc （Facebook也在用）。
             * 现在，Netty不会再因为用零填充缓冲区而浪费内存带宽了。不过，由于它不依赖于GC，开发人员需要小心内存泄漏。
             * 如果忘记在处理程序中释放缓冲区，那么内存使用率会无限地增长。
             * Netty默认不使用内存池，需要在创建客户端或者服务端的时候进行指定
             */
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

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
