/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.api.common;

import com.mpush.api.event.*;
import com.mpush.api.spi.Plugin;

/**
 * Created by ohun on 16/10/19.
 *
 * @author ohun@live.cn (夜色)
 */
public interface ServerEventListener extends Plugin {

    /**
     * 该事件通过guava EventBus发出，实现接口的方法必须增加
     *
     * <code>@Subscribe 和 @AllowConcurrentEvents</code>注解，
     * 并在构造函数调用EventBus.register(this);
     */
    default void on(ServerStartupEvent event) {
    }

    /**
     * 该事件通过guava EventBus发出，实现接口的方法必须增加
     *
     * <code>@Subscribe 和 @AllowConcurrentEvents</code>注解，
     * 并在构造函数调用EventBus.register(this);
     */
    default void on(ServerShutdownEvent server) {
    }

    /**
     * 该事件通过guava EventBus发出，实现接口的方法必须增加
     *
     * <code>@Subscribe 和 @AllowConcurrentEvents</code>注解，
     * 并在构造函数调用EventBus.register(this);
     */
    default void on(RouterChangeEvent event) {
    }

    /**
     * 该事件通过guava EventBus发出，实现接口的方法必须增加
     *
     * <code>@Subscribe 和 @AllowConcurrentEvents</code>注解，
     * 并在构造函数调用EventBus.register(this);
     */
    default void on(KickUserEvent event) {
    }

    /**
     * 该事件通过guava EventBus发出，实现接口的方法必须增加
     *
     * <code>@Subscribe 和 @AllowConcurrentEvents</code>注解，
     * 并在构造函数调用EventBus.register(this);
     */
    default void on(UserOnlineEvent event) {
    }

    /**
     * 该事件通过guava EventBus发出，实现接口的方法必须增加
     *
     * <code>@Subscribe 和 @AllowConcurrentEvents</code>注解，
     * 并在构造函数调用EventBus.register(this);
     */
    default void on(UserOfflineEvent event) {
    }
}
