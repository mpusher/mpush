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

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.api.service.Server;
import com.mpush.api.service.ServiceException;
import com.mpush.tools.thread.ThreadNames;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.socket.InternetProtocolFamily.IPv4;

/**
 * Created by ohun on 16/10/20.
 *
 * @author ohun@live.cn (夜色)
 */
public abstract class NettyUDPConnector extends BaseService implements Server {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final int port;
    private EventLoopGroup eventLoopGroup;

    public NettyUDPConnector(int port) {
        this.port = port;
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        createNioServer(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        logger.info("try shutdown {}...", this.getClass().getSimpleName());
        if (eventLoopGroup != null) eventLoopGroup.shutdownGracefully().syncUninterruptibly();
        logger.info("{} shutdown success.", this.getClass().getSimpleName());
        listener.onSuccess(port);
    }

    private void createServer(Listener listener, EventLoopGroup eventLoopGroup, ChannelFactory<? extends DatagramChannel> channelFactory) {
        this.eventLoopGroup = eventLoopGroup;
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)//默认是根据机器情况创建Channel,如果机器支持ipv6,则无法使用ipv4的地址加入组播
                    .channelFactory(channelFactory)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(getChannelHandler());

            initOptions(b);

            //直接绑定端口，不要指定host，不然收不到组播消息
            b.bind(port).addListener(future -> {
                if (future.isSuccess()) {
                    logger.info("udp server start success on:{}", port);
                    if (listener != null) listener.onSuccess(port);
                } else {
                    logger.error("udp server start failure on:{}", port, future.cause());
                    if (listener != null) listener.onFailure(future.cause());
                }
            });
        } catch (Exception e) {
            logger.error("udp server start exception", e);
            if (listener != null) listener.onFailure(e);
            throw new ServiceException("udp server start exception, port=" + port, e);
        }
    }

    private void createNioServer(Listener listener) {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(
                1, new DefaultThreadFactory(ThreadNames.T_GATEWAY_WORKER)
        );
        eventLoopGroup.setIoRatio(100);
        createServer(listener, eventLoopGroup, () -> new NioDatagramChannel(IPv4));//默认是根据机器情况创建Channel,如果机器支持ipv6,则无法使用ipv4的地址加入组播
    }

    @SuppressWarnings("unused")
    private void createEpollServer(Listener listener) {
        EpollEventLoopGroup eventLoopGroup = new EpollEventLoopGroup(
                1, new DefaultThreadFactory(ThreadNames.T_GATEWAY_WORKER)
        );
        eventLoopGroup.setIoRatio(100);
        createServer(listener, eventLoopGroup, EpollDatagramChannel::new);
    }

    protected void initOptions(Bootstrap b) {
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.option(ChannelOption.SO_REUSEADDR, true);
    }

    public abstract ChannelHandler getChannelHandler();

}
