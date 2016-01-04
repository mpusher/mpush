package com.shinemo.mpush.api.event;

import com.shinemo.mpush.api.router.Router;

/**
 * Created by ohun on 2016/1/4.
 */
public class RouterChangeEvent implements Event {
    public final String userId;
    public final Router<?> router;

    public RouterChangeEvent(String userId, Router<?> router) {
        this.userId = userId;
        this.router = router;
    }
}
