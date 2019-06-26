/*
 * (C) Copyright 2015-2017 the original author or authors.
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

package com.mpush.api;

import com.mpush.api.common.Monitor;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.api.spi.common.MQClient;
import com.mpush.api.srd.ServiceDiscovery;
import com.mpush.api.srd.ServiceRegistry;

/**
 * Created by ohun on 2017/6/21.
 *
 * MPush上下文
 *
 * @author ohun@live.cn (夜色)
 */
public interface MPushContext {

    Monitor getMonitor();

    ServiceDiscovery getDiscovery();

    ServiceRegistry getRegistry();

    CacheManager getCacheManager();

    MQClient getMQClient();

}
