package com.shinemo.mpush.netty.client;

import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by ohun on 2016/2/15.
 */
public interface HttpClient {

    void start();

    void stop();

    void request(RequestInfo requestInfo) throws Exception;
}
