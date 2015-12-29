package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/23.
 */
public interface RouterManager<R extends Router> {

    R register(String userId, R route);

    boolean unRegister(String userId);

    R getRouter(String userId);
}
