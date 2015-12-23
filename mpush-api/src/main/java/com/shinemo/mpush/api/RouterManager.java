package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/23.
 */
public interface RouterManager {

    boolean publish(long userId, Router route);

    boolean unPublish(long userId);

    Router getRouter(long userId);
}
