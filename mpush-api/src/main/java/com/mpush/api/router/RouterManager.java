/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.api.router;

import java.util.Set;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public interface RouterManager<R extends Router> {

    /**
     * 注册路由
     *
     * @param userId 用户ID
     * @param router 新路由
     * @return 如果有旧的的路由信息则返回之，否则返回空。
     */
    R register(String userId, R router);

    /**
     * 删除路由
     *
     * @param userId     用户ID
     * @param clientType 客户端类型
     * @return true:成功，false:失败
     */
    boolean unRegister(String userId, int clientType);

    /**
     * 查询路由
     *
     * @param userId 用户ID
     * @return userId对应的所有的路由信息
     */
    Set<R> lookupAll(String userId);

    /**
     * 查询指定设备类型的用户路由信息
     *
     * @param userId     用户ID
     * @param clientType 客户端类型
     * @return 指定类型的路由信息
     */
    R lookup(String userId, int clientType);
}
