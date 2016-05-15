package com.mpush.netty.client;


import io.netty.handler.codec.http.HttpResponse;

/**
 * Created by ohun on 2016/2/15.
 */
public interface HttpCallback {

    void onResponse(HttpResponse response);

    void onFailure(int statusCode, String reasonPhrase);

    void onException(Throwable throwable);

    void onTimeout();

    boolean onRedirect(HttpResponse response);
}
