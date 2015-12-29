package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/23.
 */
public interface Router<T> {

    T getRouteInfo();

    RouterType getType();

    enum RouterType {
        LOCAL, REMOTE
    }

}
