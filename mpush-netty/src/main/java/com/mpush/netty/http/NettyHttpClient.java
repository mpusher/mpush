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

package com.mpush.netty.http;

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.NamedThreadFactory;
import com.mpush.tools.thread.ThreadNames;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.mpush.tools.config.CC.mp.thread.pool.http_work;
import static com.mpush.tools.thread.ThreadNames.T_HTTP_TIMER;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

/**
 * Netty的一个Bootstrap是可以关联多个channel的，
 * 本Client采用的就是这种模式，在种模式下如果Handler添加了@ChannelHandler.Sharable
 * 注解的话，要特殊处理，因为这时的client和handler是被所有请求共享的。
 * <p>
 * Created by ohun on 2016/2/15.
 *
 * @author ohun@live.cn
 */
public class NettyHttpClient extends BaseService implements HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpClient.class);
    private static final int maxContentLength = (int) CC.mp.http.max_content_length;
    /*package*/ final AttributeKey<RequestContext> requestKey = AttributeKey.newInstance("request");
    /*package*/ final HttpConnectionPool pool = new HttpConnectionPool();
    private Bootstrap b;
    private EventLoopGroup workerGroup;
    private Timer timer;

    @Override
    public void request(RequestContext context) throws Exception {
        URI uri = new URI(context.request.uri());
        String host = context.host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();
        //1.设置请求头
        context.request.headers().set(HOST, host);//映射后的host
        context.request.headers().set(CONNECTION, KEEP_ALIVE);//保存长链接

        //2.添加请求超时检测队列
        timer.newTimeout(context, context.readTimeout, TimeUnit.MILLISECONDS);

        //3.先尝试从连接池里取可用链接，去取不到就创建新链接。
        Channel channel = pool.tryAcquire(host);
        if (channel == null) {
            final long startCreate = System.currentTimeMillis();
            LOGGER.debug("create new channel, host={}", host);
            ChannelFuture f = b.connect(host, port);
            f.addListener((ChannelFutureListener) future -> {
                LOGGER.debug("create new channel cost={}", (System.currentTimeMillis() - startCreate));
                if (future.isSuccess()) {//3.1.把请求写到http server
                    writeRequest(future.channel(), context);
                } else {//3.2如果链接创建失败，直接返回客户端网关超时
                    context.tryDone();
                    context.onFailure(504, "Gateway Timeout");
                    LOGGER.warn("create new channel failure, request={}", context);
                }
            });
        } else {
            //3.1.把请求写到http server
            writeRequest(channel, context);
        }
    }

    private void writeRequest(Channel channel, RequestContext context) {
        channel.attr(requestKey).set(context);
        pool.attachHost(context.host, channel);
        channel.writeAndFlush(context.request).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                RequestContext info = future.channel().attr(requestKey).getAndSet(null);
                info.tryDone();
                info.onFailure(503, "Service Unavailable");
                LOGGER.debug("request failure request={}", info);
                pool.tryRelease(future.channel());
            }
        });
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        workerGroup = new NioEventLoopGroup(http_work, new DefaultThreadFactory(ThreadNames.T_HTTP_CLIENT));
        b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.SO_REUSEADDR, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000);
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("decoder", new HttpResponseDecoder());
                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(maxContentLength));
                ch.pipeline().addLast("encoder", new HttpRequestEncoder());
                ch.pipeline().addLast("handler", new HttpClientHandler(NettyHttpClient.this));
            }
        });
        timer = new HashedWheelTimer(new NamedThreadFactory(T_HTTP_TIMER), 1, TimeUnit.SECONDS, 64);
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        pool.close();
        workerGroup.shutdownGracefully();
        timer.stop();
        listener.onSuccess();
    }
}
