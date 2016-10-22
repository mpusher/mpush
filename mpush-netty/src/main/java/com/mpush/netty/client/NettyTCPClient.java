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

package com.mpush.netty.client;

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Client;
import com.mpush.api.service.Listener;
import com.mpush.api.service.ServiceException;
import com.mpush.netty.codec.PacketDecoder;
import com.mpush.netty.codec.PacketEncoder;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public abstract class NettyTCPClient extends BaseService implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyTCPClient.class);

    private final String host;
    private final int port;
    private EventLoopGroup workerGroup;

    public NettyTCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void createClient(Listener listener, EventLoopGroup workerGroup, Class<? extends SocketChannel> clazz) {
        this.workerGroup = workerGroup;
        Bootstrap b = new Bootstrap();
        b.group(workerGroup)//
                .option(ChannelOption.TCP_NODELAY, true)//
                .option(ChannelOption.SO_REUSEADDR, true)//
                .option(ChannelOption.SO_KEEPALIVE, true)//
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .channel(clazz);

        b.handler(new ChannelInitializer<SocketChannel>() { // (4)
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                initPipeline(ch.pipeline());
            }
        });

        ChannelFuture future = b.connect(new InetSocketAddress(host, port));
        future.addListener(f -> {
            if (f.isSuccess()) {
                if (listener != null) listener.onSuccess(port);
                LOGGER.info("start netty client success, host={}, port={}", host, port);
            } else {
                if (listener != null) listener.onFailure(f.cause());
                LOGGER.error("start netty client failure, host={}, port={}", host, port, f.cause());
            }
        });
    }

    private void createNioClient(Listener listener) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1, getWorkExecutor());
        workerGroup.setIoRatio(getIoRate());
        createClient(listener, workerGroup, NioSocketChannel.class);
    }

    private void createEpollClient(Listener listener) {
        EpollEventLoopGroup workerGroup = new EpollEventLoopGroup(1, getWorkExecutor());
        workerGroup.setIoRatio(getIoRate());
        createClient(listener, workerGroup, EpollSocketChannel.class);
    }

    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", getDecoder());
        pipeline.addLast("encoder", getEncoder());
        pipeline.addLast("handler", getChannelHandler());
    }

    protected ChannelHandler getDecoder() {
        return new PacketDecoder();
    }

    protected ChannelHandler getEncoder() {
        return PacketEncoder.INSTANCE;
    }

    protected Executor getWorkExecutor() {
        return ThreadPoolManager.I.getWorkExecutor();
    }

    protected int getIoRate() {
        return 70;
    }

    public abstract ChannelHandler getChannelHandler();

    @Override
    protected void doStart(Listener listener) throws Throwable {
        if (CC.mp.core.useNettyEpoll()) {
            createEpollClient(listener);
        } else {
            createNioClient(listener);
        }
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        LOGGER.error("netty client [{}] stopped.", this.getClass().getSimpleName());
        listener.onSuccess();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "NettyClient{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", name=" + this.getClass().getSimpleName() +
                '}';
    }
}
