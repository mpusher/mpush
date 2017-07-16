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

import com.google.common.collect.Maps;
import com.mpush.monitor.quota.ThreadQuota;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

public class JVMThread implements ThreadQuota {

    private ThreadMXBean threadMXBean;

    public JVMThread() {
        threadMXBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public int daemonThreadCount() {
        return threadMXBean.getDaemonThreadCount();
    }

    @Override
    public int threadCount() {
        return threadMXBean.getThreadCount();
    }

    @Override
    public long totalStartedThreadCount() {
        return threadMXBean.getTotalStartedThreadCount();
    }

    @Override
    public int deadLockedThreadCount() {
        try {
            long[] deadLockedThreadIds = threadMXBean.findDeadlockedThreads();
            if (deadLockedThreadIds == null) {
                return 0;
            }
            return deadLockedThreadIds.length;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("daemonThreadCount", daemonThreadCount());
        map.put("threadCount", threadCount());
        map.put("totalStartedThreadCount", totalStartedThreadCount());
        map.put("deadLockedThreadCount", deadLockedThreadCount());
        return map;
    }
}
