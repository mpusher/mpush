package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/24.
 */
public interface Server {
    void start();

    void stop();

    boolean isRunning();
}
