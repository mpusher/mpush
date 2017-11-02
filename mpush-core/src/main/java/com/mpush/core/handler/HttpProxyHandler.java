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

package com.mpush.core.handler;

import com.google.common.base.Strings;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.api.spi.net.DnsMapping;
import com.mpush.api.spi.net.DnsMappingManager;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.HttpRequestMessage;
import com.mpush.common.message.HttpResponseMessage;
import com.mpush.core.MPushServer;
import com.mpush.netty.http.HttpCallback;
import com.mpush.netty.http.HttpClient;
import com.mpush.netty.http.RequestContext;
import com.mpush.tools.common.Profiler;
import com.mpush.tools.log.Logs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by ohun on 2016/2/15.
 *
 * @author ohun@live.cn
 */
public class HttpProxyHandler extends BaseMessageHandler<HttpRequestMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyHandler.class);
    private final DnsMappingManager dnsMappingManager = DnsMappingManager.create();
    private final HttpClient httpClient;

    public HttpProxyHandler(MPushServer mPushServer) {
        this.httpClient = mPushServer.getHttpClient();
    }

    @Override
    public HttpRequestMessage decode(Packet packet, Connection connection) {
        return new HttpRequestMessage(packet, connection);
    }

    @Override
    public void handle(HttpRequestMessage message) {
        try {
            //1.参数校验
            String method = message.getMethod();
            String uri = message.uri;
            if (Strings.isNullOrEmpty(uri)) {
                HttpResponseMessage
                        .from(message)
                        .setStatusCode(400)
                        .setReasonPhrase("Bad Request")
                        .sendRaw();
                Logs.HTTP.warn("receive bad request url is empty, request={}", message);
            }

            //2.url转换
            uri = doDnsMapping(uri);

            Profiler.enter("time cost on [create FullHttpRequest]");
            //3.包装成HTTP request
            FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.valueOf(method), uri, getBody(message));
            setHeaders(request, message);//处理header

            Profiler.enter("time cost on [HttpClient.request]");
            //4.发送请求
            httpClient.request(new RequestContext(request, new DefaultHttpCallback(message)));
        } catch (Exception e) {
            HttpResponseMessage
                    .from(message)
                    .setStatusCode(502)
                    .setReasonPhrase("Bad Gateway")
                    .sendRaw();
            LOGGER.error("send request ex, message=" + message, e);
            Logs.HTTP.error("send proxy request ex, request={}, error={}", message, e.getMessage());
        } finally {
            Profiler.release();
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
                    .setReasonPhrase(httpResponse.status().reasonPhrase());
            for (Map.Entry<String, String> entry : httpResponse.headers()) {
                response.addHeader(entry.getKey(), entry.getValue());
            }

            if (httpResponse instanceof FullHttpResponse) {
                ByteBuf content = ((FullHttpResponse) httpResponse).content();
                if (content != null && content.readableBytes() > 0) {
                    byte[] body = new byte[content.readableBytes()];
                    content.readBytes(body);
                    response.body = body;
                    response.addHeader(CONTENT_LENGTH.toString(), Integer.toString(response.body.length));
                }
            }
            response.send();
            Logs.HTTP.info("send proxy request success end request={}, response={}", request, response);
        }

        @Override
        public void onFailure(int statusCode, String reasonPhrase) {
            HttpResponseMessage
                    .from(request)
                    .setStatusCode(statusCode)
                    .setReasonPhrase(reasonPhrase)
                    .sendRaw();
            Logs.HTTP.warn("send proxy request failure end request={}, response={}", request, statusCode + ":" + reasonPhrase);
        }

        @Override
        public void onException(Throwable throwable) {
            HttpResponseMessage
                    .from(request)
                    .setStatusCode(502)
                    .setReasonPhrase("Bad Gateway")
                    .sendRaw();

            LOGGER.error("send proxy request ex end request={}, response={}", request, 502, throwable);
            Logs.HTTP.error("send proxy request ex end request={}, response={}, error={}", request, 502, throwable.getMessage());
        }

        @Override
        public void onTimeout() {
            HttpResponseMessage
                    .from(request)
                    .setStatusCode(408)
                    .setReasonPhrase("Request Timeout")
                    .sendRaw();
            Logs.HTTP.warn("send proxy request timeout end request={}, response={}", request, 408);
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

        if (message.body != null && message.body.length > 0) {
            request.headers().add(CONTENT_LENGTH, Integer.toString(message.body.length));
        }

        InetSocketAddress remoteAddress = (InetSocketAddress) message.getConnection().getChannel().remoteAddress();
        String remoteIp = remoteAddress.getAddress().getHostAddress();//这个要小心，不要使用getHostName,不然会耗时比较大
        request.headers().add("x-forwarded-for", remoteIp);
        request.headers().add("x-forwarded-port", Integer.toString(remoteAddress.getPort()));
    }

    private ByteBuf getBody(HttpRequestMessage message) {
        return message.body == null ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(message.body);
    }

    private String doDnsMapping(String url) {
        URL uri = null;
        try {
            uri = new URL(url);
        } catch (MalformedURLException e) {
            //ignore e
        }
        if (uri == null) {
            return url;
        }
        String host = uri.getHost();
        DnsMapping mapping = dnsMappingManager.lookup(host);
        if (mapping == null) {
            return url;
        }
        return mapping.translate(uri);
    }
}
