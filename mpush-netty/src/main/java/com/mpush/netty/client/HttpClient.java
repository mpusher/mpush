package com.mpush.netty.client;

/**
 * Created by ohun on 2016/2/15.
 *
 * @author ohun@live.cn
 */
public interface HttpClient {

    void start();

    void stop();

    void request(RequestInfo requestInfo) throws Exception;
}
