package com.shinemo.mpush.netty.client;

import com.google.common.collect.ArrayListMultimap;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.tools.thread.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.HashedWheelTimer;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ohun on 2016/2/15.
 */
public class NettyHttpClient implements HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpClient.class);
    private Bootstrap b;
    private EventLoopGroup workerGroup;
    private Timer timer;
    private final AttributeKey<RequestInfo> key = AttributeKey.newInstance("requestInfo");
    private final ArrayListMultimap<String, Channel> channelPool = ArrayListMultimap.create();

    @Override
    public void start() { // TODO: 2016/2/15 yxx 配置线程池
        workerGroup = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("decoder", new HttpResponseDecoder());
                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 20));
                ch.pipeline().addLast("encoder", new HttpRequestEncoder());
                ch.pipeline().addLast("handler", new HttpClientHandler());
            }
        });
        timer = new HashedWheelTimer(new NamedThreadFactory("http-client-timer-"),
                1, TimeUnit.SECONDS, 64);
    }

    @Override
    public void stop() {
        for (Channel channel : channelPool.values()) {
            channel.close();
        }
        channelPool.clear();
        workerGroup.shutdownGracefully();
        timer.stop();
    }

    @Override
    public void request(final RequestInfo info) throws Exception {
        HttpRequest request = info.request;
        String url = request.uri();
        URI uri = new URI(url);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();
        info.host = host;
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        timer.newTimeout(info, info.timeout, TimeUnit.MILLISECONDS);
        Channel channel = tryAcquire(host);
        if (channel == null) {
            LOGGER.debug("create new channel,host=" + host);
            ChannelFuture f = b.connect(host, port);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        writeRequest(future.channel(), info);
                    } else {
                        info.cancel();
                        info.callback.onFailure(504, "Gateway Timeout");
                    }
                }
            });
        } else {
            writeRequest(channel, info);
        }
    }

    private synchronized Channel tryAcquire(String host) {
        List<Channel> channels = channelPool.get(host);
        if (channels == null || channels.isEmpty()) return null;
        Iterator<Channel> it = channels.iterator();
        while (it.hasNext()) {
            Channel channel = it.next();
            channelPool.remove(host, channel);
            if (channel.isActive()) {
                LOGGER.debug("tryAcquire channel success ,host=" + host);
                return channel;
            }
        }
        return null;
    }

    private synchronized void tryRelease(Channel channel) {
        String host = channel.attr(key).getAndRemove().host;
        List<Channel> channels = channelPool.get(host);
        if (channels == null || channels.size() < 5) {
            LOGGER.debug("tryRelease channel success ,host=" + host);
            channelPool.put(host, channel);
        } else {
            LOGGER.debug("tryRelease channel failure ,host=" + host);
            channel.close();
        }
    }

    private void writeRequest(Channel channel, RequestInfo info) {
        channel.attr(key).set(info);
        channel.writeAndFlush(info.request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    RequestInfo requestInfo = future.channel().attr(key).get();
                    requestInfo.cancel();
                    requestInfo.callback.onFailure(503, "Service Unavailable");
                    tryRelease(future.channel());
                }
            }
        });
    }


    private class HttpClientHandler extends ChannelHandlerAdapter {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LOGGER.error("http client caught an error,", cause);
            try {
                RequestInfo info = ctx.channel().attr(key).get();
                if (info.cancel()) {
                    info.callback.onException(cause);
                }
            } finally {
                tryRelease(ctx.channel());
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            RequestInfo info = ctx.channel().attr(key).get();
            if (info == null) return;
            try {
                if (info.cancel()) {
                    HttpCallback callback = info.callback;
                    HttpRequest request = info.request;
                    HttpResponse response = (HttpResponse) msg;
                    if (isRedirect(response)) {
                        if (callback.onRedirect(response)) {
                            CharSequence location = getRedirectLocation(request, response);
                            if (location != null && location.length() > 0) {
                                info.cancelled.set(false);
                                info.request = copy(location.toString(), request);
                                request(info);
                                return;
                            }
                        }
                    }
                    callback.onResponse(response);
                }
            } finally {
                tryRelease(ctx.channel());
            }
        }

        private boolean isRedirect(HttpResponse response) {
            HttpResponseStatus status = response.status();
            switch (status.code()) {
                case 300:
                case 301:
                case 302:
                case 303:
                case 305:
                case 307:
                    return true;
                default:
                    return false;
            }
        }

        private String getRedirectLocation(HttpRequest request, HttpResponse response) throws Exception {
            String hdr = URLDecoder.decode(response.headers().get(HttpHeaderNames.LOCATION).toString(), "UTF-8");
            if (hdr != null) {
                if (hdr.toLowerCase().startsWith("http://") || hdr.toLowerCase().startsWith("https://")) {
                    return hdr;
                } else {
                    URL orig = new URL(request.uri());
                    String pth = orig.getPath() == null ? "/" : URLDecoder.decode(orig.getPath().toString(), "UTF-8");
                    if (hdr.startsWith("/")) {
                        pth = hdr;
                    } else if (pth.endsWith("/")) {
                        pth += hdr;
                    } else {
                        pth += "/" + hdr;
                    }
                    StringBuilder sb = new StringBuilder(orig.getProtocol().toString());
                    sb.append("://").append(orig.getHost());
                    if (orig.getPort() > 0) {
                        sb.append(":").append(orig.getPort());
                    }
                    if (pth.charAt(0) != '/') {
                        sb.append('/');
                    }
                    sb.append(pth);
                    return sb.toString();
                }
            }
            return null;
        }


        private HttpRequest copy(String uri, HttpRequest request) {
            HttpRequest nue = request;
            if (request instanceof DefaultFullHttpRequest) {
                DefaultFullHttpRequest dfrq = (DefaultFullHttpRequest) request;
                FullHttpRequest rq;
                try {
                    rq = dfrq.copy();
                } catch (IllegalReferenceCountException e) { // Empty bytebuf
                    rq = dfrq;
                }
                rq.setUri(uri);
            } else {
                DefaultHttpRequest dfr = new DefaultHttpRequest(request.protocolVersion(), request.method(), uri);
                dfr.headers().set(request.headers());
                nue = dfr;
            }
            return nue;
        }

    }


    public static void main(String[] args) throws Exception {
        NettyHttpClient client = new NettyHttpClient();
        client.start();
        for (int i = 0; i < 100; i++) {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://baidu.com/");
            client.request(new RequestInfo(request, new HttpCallback() {
                @Override
                public void onResponse(HttpResponse response) {
                    System.out.println("response=" + response);
                    //System.out.println("content=" + ((FullHttpResponse) response).content().toString(Constants.UTF_8));
                }

                @Override
                public void onFailure(int statusCode, String reasonPhrase) {
                    System.out.println("reasonPhrase=" + reasonPhrase);
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onTimeout() {
                    System.out.println("onTimeout");
                }

                @Override
                public boolean onRedirect(HttpResponse response) {
                    return true;
                }
            }));
        }
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
        client.stop();
    }
}
