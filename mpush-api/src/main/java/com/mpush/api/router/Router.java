package com.mpush.api.router;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public interface Router<T> {

    T getRouteValue();

    RouterType getRouteType();

    enum RouterType {
        LOCAL, REMOTE
    }

}
