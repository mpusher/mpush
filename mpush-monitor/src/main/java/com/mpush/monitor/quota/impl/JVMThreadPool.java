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

package com.mpush.monitor.quota.impl;

import com.mpush.monitor.quota.ThreadPoolQuota;
import com.mpush.tools.thread.pool.ThreadPoolManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class JVMThreadPool implements ThreadPoolQuota {
    public static final JVMThreadPool I = new JVMThreadPool();

    private JVMThreadPool() {
    }


    @Override
    public Object monitor(Object... args) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Executor> pool = ThreadPoolManager.I.getActivePools();
        for (Map.Entry<String, Executor> entry : pool.entrySet()) {
            String serviceName = entry.getKey();
            Executor executor = entry.getValue();
            if (executor instanceof ThreadPoolExecutor) {
                map.put(serviceName, ThreadPoolManager.getPoolInfo((ThreadPoolExecutor) executor));
            }
        }
        return map;
    }
}
