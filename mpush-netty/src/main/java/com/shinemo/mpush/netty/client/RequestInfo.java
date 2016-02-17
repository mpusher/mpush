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
    int timeout = 10000;//5s
    long startTime = System.currentTimeMillis();
    long endTime = System.currentTimeMillis();

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
        if (tryDone()) {
            callback.onTimeout();
        }
    }

    public boolean tryDone() {
        endTime = System.currentTimeMillis();
        return cancelled.compareAndSet(false, true);
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "cancelled=" + cancelled +
                ", request=" + request +
                ", host='" + host + '\'' +
                ", timeout=" + timeout +
                ", costTime=" + (startTime - endTime) +
                '}';
    }
}