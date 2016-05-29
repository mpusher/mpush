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
import com.sun.istack.internal.NotNull;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public abstract class NettyClient extends BaseService implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    private final String host;
    private final int port;
    private EventLoopGroup workerGroup;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start(final Listener listener) {
        if (started.compareAndSet(false, true)) {
            Bootstrap bootstrap = new Bootstrap();
            workerGroup = new NioEventLoopGroup();
            bootstrap.group(workerGroup)//
                    .option(ChannelOption.TCP_NODELAY, true)//
                    .option(ChannelOption.SO_REUSEADDR, true)//
                    .option(ChannelOption.SO_KEEPALIVE, true)//
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000);

            bootstrap.handler(new ChannelInitializer<SocketChannel>() { // (4)
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    initPipeline(ch.pipeline());
                }
            });

            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        if (listener != null) listener.onSuccess(port);
                        LOGGER.info("start netty client success, host={}, port={}", host, port);
                    } else {
                        if (listener != null) listener.onFailure(future.cause());
                        LOGGER.error("start netty client failure, host={}, port={}", host, port, future.cause());
                    }
                }
            });
        } else {
            listener.onFailure(new ServiceException("client has started!"));
        }
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


    public abstract ChannelHandler getChannelHandler();

    @Override
    protected void doStart(@NotNull Listener listener) throws Throwable {

    }

    @Override
    protected void doStop(@NotNull Listener listener) throws Throwable {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        LOGGER.error("netty client [{}] stopped.", this.getClass().getSimpleName());
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
