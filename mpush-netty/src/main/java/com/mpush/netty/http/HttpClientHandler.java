package com.mpush.netty.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLDecoder;

@ChannelHandler.Sharable
public class HttpClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpClient.class);
    private final NettyHttpClient client;

    public HttpClientHandler(NettyHttpClient client) {
        this.client = client;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        RequestContext context = ctx.channel().attr(client.requestKey).getAndRemove();
        try {
            if (context != null && context.tryDone()) {
                context.onException(cause);
            }
        } finally {
            client.pool.tryRelease(ctx.channel());
        }
        LOGGER.error("http client caught an error, info={}", context, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestContext context = ctx.channel().attr(client.requestKey).getAndRemove();
        try {
            if (context != null && context.tryDone()) {
                HttpResponse response = (HttpResponse) msg;
                if (isRedirect(response)) {
                    if (context.onRedirect(response)) {
                        String location = getRedirectLocation(context.request, response);
                        if (location != null && location.length() > 0) {
                            context.cancelled.set(false);
                            context.request = context.request.copy().setUri(location);
                            client.request(context);
                            return;
                        }
                    }
                }
                context.onResponse(response);
                LOGGER.debug("request done request={}", context);
            }
        } finally {
            client.pool.tryRelease(ctx.channel());
            ReferenceCountUtil.release(msg);
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