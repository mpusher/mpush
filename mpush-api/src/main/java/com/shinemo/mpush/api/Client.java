package com.shinemo.mpush.api;

public interface Client {

    void init();

    void start();

    void close(final String cause);

    boolean isEnabled();

    boolean isConnected();

    void resetHbTimes();

    int inceaseAndGetHbTimes();

    String getHost();

    int getPort();

    String getUri();

}
