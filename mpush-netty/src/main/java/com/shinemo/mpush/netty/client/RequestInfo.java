package com.shinemo.mpush.netty.client;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.atomic.AtomicBoolean;

public class RequestInfo implements TimerTask {
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    HttpCallback callback;
    HttpRequest request;
    String host;
    int timeout = 5000;//5s

    public RequestInfo(HttpRequest request, HttpCallback callback) {
        this.callback = callback;
        this.request = request;
    }

    public HttpCallback getCallback() {
        return callback;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public int getTimeout() {
        return timeout;
    }

    public RequestInfo setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        if (!cancelled.get()) {
            cancelled.set(true);
            callback.onTimeout();
        }
    }

    public boolean cancel() {
        return cancelled.compareAndSet(false, true);
    }
}