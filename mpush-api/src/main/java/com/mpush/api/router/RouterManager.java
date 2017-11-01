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
     * @param userId
     * @param router
     * @return
     */
    R register(String userId, R router);

    /**
     * 删除路由
     *
     * @param userId
     * @param clientType
     * @return
     */
    boolean unRegister(String userId, int clientType);

    /**
     * 查询路由
     *
     * @param userId
     * @return
     */
    Set<R> lookupAll(String userId);

    /**
     * 查询路由
     *
     * @param userId
     * @param clientType
     * @return
     */
    R lookup(String userId, int clientType);
}
