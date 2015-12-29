package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/23.
 */
public interface RouterManager<R extends Router> {

    /**
     * 注册路由
     *
     * @param userId
     * @param route
     * @return
     */
    R register(String userId, R route);

    /**
     * 删除路由
     *
     * @param userId
     * @return
     */
    boolean unRegister(String userId);

    /**
     * 查询路由
     *
     * @param userId
     * @return
     */
    R lookup(String userId);
}
