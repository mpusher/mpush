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

package com.mpush.netty.server;

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.api.service.Server;
import com.mpush.api.service.ServiceException;
import com.mpush.netty.codec.PacketDecoder;
import com.mpush.netty.codec.PacketEncoder;
import com.mpush.tools.common.Strings;
import com.mpush.tools.thread.ThreadNames;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import static com.mpush.tools.Utils.useNettyEpoll;

/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn
 */
public abstract class NettyTCPServer extends BaseService implements Server {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum State {Created, Initialized, Starting, Started, Shutdown}

    protected final AtomicReference<State> serverState = new AtomicReference<>(State.Created);

    protected final int port;
    protected final String host;
    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;

    public NettyTCPServer(int port) {
        this.port = port;
        this.host = null;
    }

    public NettyTCPServer(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void init() {
        if (!serverState.compareAndSet(State.Created, State.Initialized)) {
            throw new ServiceException("Server already init");
        }
    }

    @Override
    public boolean isRunning() {
        return serverState.get() == State.Started;
    }

    @Override
    public void stop(Listener listener) {
        if (!serverState.compareAndSet(State.Started, State.Shutdown)) {
            if (listener != null) listener.onFailure(new ServiceException("server was already shutdown."));
            logger.error("{} was already shutdown.", this.getClass().getSimpleName());
            return;
        }
        logger.info("try shutdown {}...", this.getClass().getSimpleName());
        if (bossGroup != null) bossGroup.shutdownGracefully().syncUninterruptibly();//要先关闭接收连接的main reactor
        if (workerGroup != null) workerGroup.shutdownGracefully().syncUninterruptibly();//再关闭处理业务的sub reactor
        logger.info("{} shutdown success.", this.getClass().getSimpleName());
        if (listener != null) {
            listener.onSuccess(port);
        }
    }

    @Override
    public void start(final Listener listener) {
        if (!serverState.compareAndSet(State.Initialized, State.Starting)) {
            throw new ServiceException("Server already started or have not init");
        }
        if (useNettyEpoll()) {
            createEpollServer(listener);
        } else {
            createNioServer(listener);
        }
    }

    private void createServer(Listener listener, EventLoopGroup boss, EventLoopGroup work, ChannelFactory<? extends ServerChannel> channelFactory) {
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
            b.channelFactory(channelFactory);


            /***
             * 这里的事件处理类经常会被用来处理一个最近的已经接收的Channel。
             * ChannelInitializer是一个特殊的处理类，
             * 他的目的是帮助使用者配置一个新的Channel。
             * 也许你想通过增加一些处理类比如NettyServerHandler来配置一个新的Channel
             * 或者其对应的ChannelPipeline来实现你的网络程序。
             * 当你的程序变的复杂时，可能你会增加更多的处理类到pipeline上，
             * 然后提取这些匿名类到最顶层的类上。
             */
            b.childHandler(new ChannelInitializer<Channel>() { // (4)
                @Override
                public void initChannel(Channel ch) throws Exception {//每连上一个链接调用一次
                    initPipeline(ch.pipeline());
                }
            });

            initOptions(b);

            /***
             * 绑定端口并启动去接收进来的连接
             */
            InetSocketAddress address = Strings.isBlank(host) ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
            b.bind(address).addListener(future -> {
                if (future.isSuccess()) {
                    serverState.set(State.Started);
                    logger.info("server start success on:{}", port);
                    if (listener != null) listener.onSuccess(port);
                } else {
                    logger.error("server start failure on:{}", port, future.cause());
                    if (listener != null) listener.onFailure(future.cause());
                }
            });
        } catch (Exception e) {
            logger.error("server start exception", e);
            if (listener != null) listener.onFailure(e);
            throw new ServiceException("server start exception, port=" + port, e);
        }
    }

    private void createNioServer(Listener listener) {
        EventLoopGroup bossGroup = getBossGroup();
        EventLoopGroup workerGroup = getWorkerGroup();

        if (bossGroup == null) {
            NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(getBossThreadNum(), getBossThreadFactory(), getSelectorProvider());
            nioEventLoopGroup.setIoRatio(100);
            bossGroup = nioEventLoopGroup;
        }

        if (workerGroup == null) {
            NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(getWorkThreadNum(), getWorkThreadFactory(), getSelectorProvider());
            nioEventLoopGroup.setIoRatio(getIoRate());
            workerGroup = nioEventLoopGroup;
        }

        createServer(listener, bossGroup, workerGroup, getChannelFactory());
    }

    private void createEpollServer(Listener listener) {
        EventLoopGroup bossGroup = getBossGroup();
        EventLoopGroup workerGroup = getWorkerGroup();

        if (bossGroup == null) {
            EpollEventLoopGroup epollEventLoopGroup = new EpollEventLoopGroup(getBossThreadNum(), getBossThreadFactory());
            epollEventLoopGroup.setIoRatio(100);
            bossGroup = epollEventLoopGroup;
        }

        if (workerGroup == null) {
            EpollEventLoopGroup epollEventLoopGroup = new EpollEventLoopGroup(getWorkThreadNum(), getWorkThreadFactory());
            epollEventLoopGroup.setIoRatio(getIoRate());
            workerGroup = epollEventLoopGroup;
        }

        createServer(listener, bossGroup, workerGroup, EpollServerSocketChannel::new);
    }

    /***
     * option()是提供给NioServerSocketChannel用来接收进来的连接。
     * childOption()是提供给由父管道ServerChannel接收到的连接，
     * 在这个例子中也是NioServerSocketChannel。
     */
    protected void initOptions(ServerBootstrap b) {
        //b.childOption(ChannelOption.SO_KEEPALIVE, false);// 使用应用层心跳

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

    protected ChannelHandler getDecoder() {
        return new PacketDecoder();
    }

    protected ChannelHandler getEncoder() {
        return PacketEncoder.INSTANCE;//每连上一个链接调用一次, 所有用单利
    }

    /**
     * 每连上一个链接调用一次
     *
     * @param pipeline
     */
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", getDecoder());
        pipeline.addLast("encoder", getEncoder());
        pipeline.addLast("handler", getChannelHandler());
    }

    /**
     * netty 默认的Executor为ThreadPerTaskExecutor
     * 线程池的使用在SingleThreadEventExecutor#doStartThread
     * <p>
     * eventLoop.execute(runnable);
     * 是比较重要的一个方法。在没有启动真正线程时，
     * 它会启动线程并将待执行任务放入执行队列里面。
     * 启动真正线程(startThread())会判断是否该线程已经启动，
     * 如果已经启动则会直接跳过，达到线程复用的目的
     *
     * @return
     */
    protected ThreadFactory getBossThreadFactory() {
        return new DefaultThreadFactory(getBossThreadName());
    }

    protected ThreadFactory getWorkThreadFactory() {
        return new DefaultThreadFactory(getWorkThreadName());
    }

    protected int getBossThreadNum() {
        return 1;
    }

    protected int getWorkThreadNum() {
        return 0;
    }

    protected String getBossThreadName() {
        return ThreadNames.T_BOSS;
    }

    protected String getWorkThreadName() {
        return ThreadNames.T_WORKER;
    }

    protected int getIoRate() {
        return 70;
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public ChannelFactory<? extends ServerChannel> getChannelFactory() {
        return NioServerSocketChannel::new;
    }

    public SelectorProvider getSelectorProvider() {
        return SelectorProvider.provider();
    }
}
