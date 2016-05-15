package com.mpush.api.event;

import com.mpush.api.router.Router;

/**
 * Created by ohun on 2016/1/4.
 *
 * @author ohun@live.cn
 */
public final class RouterChangeEvent implements Event {
    public final String userId;
    public final Router<?> router;

    public RouterChangeEvent(String userId, Router<?> router) {
        this.userId = userId;
        this.router = router;
    }
}
