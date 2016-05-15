package com.mpush.api.router;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public interface RouterManager<R extends Router> {

    /**
     * 注册路由
     *
     * @param userId
     * @param router
     * @return
     */
    R register(String userId, R router);

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
