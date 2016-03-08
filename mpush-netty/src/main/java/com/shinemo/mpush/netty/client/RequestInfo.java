package com.shinemo.mpush.netty.client;

import com.google.common.primitives.Ints;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.tools.config.ConfigCenter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.atomic.AtomicBoolean;

public class RequestInfo implements TimerTask, HttpCallback {
    private static final int TIMEOUT = ConfigCenter.holder.httpDefaultReadTimeout();
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    final long startTime = System.currentTimeMillis();
    long endTime = startTime;
    int readTimeout = TIMEOUT;
    private String uri;
    private HttpCallback callback;
    FullHttpRequest request;
    String host;

    public RequestInfo(FullHttpRequest request, HttpCallback callback) {
        this.callback = callback;
        this.request = request;
        this.uri = request.uri();
        String timeout = request.headers().getAndRemoveAndConvert(Constants.HTTP_HEAD_READ_TIMEOUT);
        if (timeout != null) {
            Integer integer = Ints.tryParse(timeout);
            if (integer != null && integer > 0) readTimeout = integer;
        }
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public RequestInfo setReadTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        if (tryDone()) {
            if (callback != null) {
                callback.onTimeout();
            }
        }
    }

    public boolean tryDone() {
        return cancelled.compareAndSet(false, true);
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "cancelled=" + cancelled +
                ", uri='" + uri + '\'' +
                ", host='" + host + '\'' +
                ", readTimeout=" + readTimeout +
                ", costTime=" + (endTime - startTime) +
                '}';
    }

    @Override
    public void onResponse(HttpResponse response) {
        callback.onResponse(response);
        endTime = System.currentTimeMillis();
        destroy();
    }

    @Override
    public void onFailure(int statusCode, String reasonPhrase) {
        callback.onFailure(statusCode, reasonPhrase);
        endTime = System.currentTimeMillis();
        destroy();
    }

    @Override
    public void onException(Throwable throwable) {
        callback.onException(throwable);
        endTime = System.currentTimeMillis();
        destroy();
    }

    @Override
    public void onTimeout() {
        callback.onTimeout();
        endTime = System.currentTimeMillis();
        destroy();
    }

    @Override
    public boolean onRedirect(HttpResponse response) {
        endTime = System.currentTimeMillis();
        return callback.onRedirect(response);
    }

    private void destroy() {
        request = null;
        callback = null;
    }
}