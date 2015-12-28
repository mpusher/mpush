package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/23.
 */
public interface RouterManager {

    boolean publish(String userId, Router route);

    boolean unPublish(String userId);

    Router getRouter(String userId);
}
