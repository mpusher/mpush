package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Receiver {
    void onMessage(Request request);
}
