package com.shinemo.mpush.netty.server;

import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.netty.codec.PacketDecoder;
import com.shinemo.mpush.netty.codec.PacketEncoder;
import com.shinemo.mpush.tools.thread.threadpool.ThreadPoolManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ohun on 2015/12/22.
 */
public abstract class NettyServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    public enum State {Created, Initialized, Starting, Started, Shutdown}

	protected final AtomicReference<State> serverState = new AtomicReference<>(State.Created);

    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(int port) {
        this.port = port;
        
    }

    public void init() {
        if (!serverState.compareAndSet(State.Created, State.Initialized)) {
            throw new IllegalStateException("Server already init");
        }
    }

    @Override
    public boolean isRunning() {
        return serverState.get() == State.Started;
    }

    @Override
    public void stop(Listener listener) {
        if (!serverState.compareAndSet(State.Started, State.Shutdown)) {
            throw new IllegalStateException("The server is already shutdown.");
        }
        if (workerGroup != null) workerGroup.shutdownGracefully().syncUninterruptibly();
        if (bossGroup != null) bossGroup.shutdownGracefully().syncUninterruptibly();
        LOGGER.warn("netty server stop now");
    }

    @Override
    public void start(final Listener listener) {
        if (!serverState.compareAndSet(State.Initialized, State.Starting)) {
            throw new IllegalStateException("Server already started or have not init");
        }
        createNioServer(listener);
    }

    private void createServer(final Listener listener, EventLoopGroup boss, EventLoopGroup work, Class<? extends ServerChannel> clazz) {
        /***
         * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
         * Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议。
         * 在一个服务端的应用会有2个NioEventLoopGroup会被使用。
         * 第一个经常被叫做‘boss’，用来接收进来的连接。
         * 第二个经常被叫做‘worker’，用来处理已经被接收的连接，
         * 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
         * 如何知道多少个线程已经被使用，如何映射到已经创建的Channels上都需要依赖于EventLoopGroup的实现，
         * 并且可以通过构造函数来配置他们的关系。
         */
        this.bossGroup = boss;
        this.workerGroup = work;

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
            b.channel(clazz);


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
                    ch.pipeline().addLast(getChannelHandler());
                }
            });

            initOptions(b);

            /***
             * 绑定端口并启动去接收进来的连接
             */
            ChannelFuture f = b.bind(port).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        LOGGER.info("server start success on:" + port);
                        if (listener != null) listener.onSuccess();
                    } else {
                        LOGGER.error("server start failure on:" + port);
                        if (listener != null) listener.onFailure("start server failure");
                    }
                }
            });
            serverState.set(State.Started);
            /**
             * 这里会一直等待，直到socket被关闭
             */
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("server start exception", e);
            if (listener != null) listener.onFailure("start server ex=" + e.getMessage());
            throw new RuntimeException("server start exception, port=" + port, e);
        } finally {
            /***
             * 优雅关闭
             */
            stop(null);
        }
    }

    private void createNioServer(final Listener listener) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1,ThreadPoolManager.bossExecutor);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(0, ThreadPoolManager.workExecutor);
        createServer(listener, bossGroup, workerGroup, NioServerSocketChannel.class);
    }


    @SuppressWarnings("unused")
	private void createEpollServer(final Listener listener) {
        EpollEventLoopGroup bossGroup = new EpollEventLoopGroup(1, ThreadPoolManager.bossExecutor);
        EpollEventLoopGroup workerGroup = new EpollEventLoopGroup(0, ThreadPoolManager.workExecutor);
        createServer(listener, bossGroup, workerGroup, EpollServerSocketChannel.class);
    }

    protected void initOptions(ServerBootstrap b) {

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
    }


    public abstract ChannelHandler getChannelHandler();
}
