package com.shinemo.mpush.api;

public interface Client {

    void init();

    void start();

    void stop();

    boolean isConnected();

    String getUri();

}
