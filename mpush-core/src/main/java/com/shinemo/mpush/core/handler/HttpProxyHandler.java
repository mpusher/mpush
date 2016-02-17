package com.shinemo.mpush.core.handler;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.DnsMapping;
import com.shinemo.mpush.common.handler.BaseMessageHandler;
import com.shinemo.mpush.common.message.HttpRequestMessage;
import com.shinemo.mpush.common.message.HttpResponseMessage;
import com.shinemo.mpush.netty.client.HttpCallback;
import com.shinemo.mpush.netty.client.NettyHttpClient;
import com.shinemo.mpush.netty.client.RequestInfo;
import com.shinemo.mpush.tools.MPushUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by ohun on 2016/2/15.
 */
public class HttpProxyHandler extends BaseMessageHandler<HttpRequestMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyHandler.class);
    private final NettyHttpClient httpClient;
    private final DnsMapping dnsMapping;

    public HttpProxyHandler() {
        this.httpClient = new NettyHttpClient();
        this.dnsMapping = new DnsMapping();
        this.httpClient.start();
    }

    @Override
    public HttpRequestMessage decode(Packet packet, Connection connection) {
        return new HttpRequestMessage(packet, connection);
    }

    @Override
    public void handle(HttpRequestMessage message) {
        String method = message.getMethod();
        String uri = message.uri;
        if (Strings.isNullOrEmpty(uri)) {
            HttpResponseMessage
                    .from(message)
                    .setStatusCode(400)
                    .setReasonPhrase("Bad Request")
                    .sendRaw();
            LOGGER.warn("request url is empty!");
        }

        uri = doDnsMapping(uri);
        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.valueOf(method), uri);
        setHeaders(request, message);
        setBody(request, message);

        try {
            httpClient.request(new RequestInfo(request, new DefaultHttpCallback(message)));
        } catch (Exception e) {
            HttpResponseMessage
                    .from(message)
                    .setStatusCode(500)
                    .setReasonPhrase("Internal Server Error")
                    .sendRaw();
            LOGGER.error("send request ex, message=" + message, e);
        }
    }

    private static class DefaultHttpCallback implements HttpCallback {
        private final HttpRequestMessage request;
        private int redirectCount;

        private DefaultHttpCallback(HttpRequestMessage request) {
            this.request = request;
        }

        @Override
        public void onResponse(HttpResponse httpResponse) {
            HttpResponseMessage response = HttpResponseMessage
                    .from(request)
                    .setStatusCode(httpResponse.status().code())
                    .setReasonPhrase(httpResponse.status().reasonPhrase().toString());
            for (Map.Entry<CharSequence, CharSequence> entry : httpResponse.headers()) {
                response.addHeader(entry.getKey().toString(), entry.getValue().toString());
            }

            if (httpResponse instanceof FullHttpResponse) {
                ByteBuf body = ((FullHttpResponse) httpResponse).content();
                if (body != null && body.readableBytes() > 0) {
                    byte[] buffer = new byte[body.readableBytes()];
                    body.readBytes(buffer);
                    response.body = buffer;
                    response.addHeader(HttpHeaderNames.CONTENT_LENGTH.toString(),
                            Integer.toString(response.body.length));
                }
            }
            response.send();
            LOGGER.debug("callback success request={}, response={}", request, response);
        }

        @Override
        public void onFailure(int statusCode, String reasonPhrase) {
            HttpResponseMessage
                    .from(request)
                    .setStatusCode(statusCode)
                    .setReasonPhrase(reasonPhrase)
                    .sendRaw();
            LOGGER.warn("callback failure request={}, response={}", request, statusCode + ":" + reasonPhrase);
        }

        @Override
        public void onException(Throwable throwable) {
            HttpResponseMessage
                    .from(request)
                    .setStatusCode(500)
                    .setReasonPhrase("Internal Server Error")
                    .sendRaw();
            LOGGER.error("callback exception request={}, response={}", request, 500, throwable);
        }

        @Override
        public void onTimeout() {
            HttpResponseMessage
                    .from(request)
                    .setStatusCode(408)
                    .setReasonPhrase("Request Timeout")
                    .sendRaw();
            LOGGER.warn("callback timeout request={}, response={}", request, 408);
        }

        @Override
        public boolean onRedirect(HttpResponse response) {
            return redirectCount++ < 5;
        }
    }


    private void setHeaders(FullHttpRequest request, HttpRequestMessage message) {
        Map<String, String> headers = message.headers;
        if (headers != null) {
            HttpHeaders httpHeaders = request.headers();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        InetSocketAddress remoteAddress = (InetSocketAddress) message.getConnection().getChannel().remoteAddress();
        request.headers().add("x-forwarded-for", remoteAddress.getHostName() + "," + MPushUtil.getLocalIp());
        request.headers().add("x-forwarded-port", Integer.toString(remoteAddress.getPort()));
    }

    private void setBody(FullHttpRequest request, HttpRequestMessage message) {
        byte[] body = message.body;
        if (body != null && body.length > 0) {
            request.content().writeBytes(body);
            request.headers().add(HttpHeaderNames.CONTENT_LENGTH,
                    Integer.toString(body.length));
        }
    }

    private String doDnsMapping(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
        }
        if (uri == null) return url;
        String host = uri.getHost();
        String localHost = dnsMapping.translate(host);
        if (localHost == null) return url;
        return url.replace(host, localHost);
    }
}
