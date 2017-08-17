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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mpush.monitor.quota.GCMQuota;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;

public class JVMGC implements GCMQuota {

    private final List<String> fullGcName = Lists.newArrayList("ConcurrentMarkSweep", "MarkSweepCompact", "PS MarkSweep", "G1 Old Generation",
            "Garbage collection optimized for short pausetimes Old Collector", "Garbage collection optimized for throughput Old Collector",
            "Garbage collection optimized for deterministic pausetimes Old Collector");

    private final List<String> youngGcName = Lists.newArrayList("ParNew", "Copy", "PS Scavenge", "G1 Young Generation", "Garbage collection optimized for short pausetimes Young Collector",
            "Garbage collection optimized for throughput Young Collector", "Garbage collection optimized for deterministic pausetimes Young Collector");

    private GarbageCollectorMXBean fullGc;
    private GarbageCollectorMXBean yongGc;

    private long lastYoungGcCollectionCount = -1;
    private long lastYoungGcCollectionTime = -1;
    private long lastFullGcCollectionCount = -1;
    private long lastFullGcCollectionTime = -1;


    public JVMGC() {
        for (GarbageCollectorMXBean item : ManagementFactory.getGarbageCollectorMXBeans()) {
            String name = item.getName();
            if (youngGcName.contains(name)) {
                yongGc = item;
            } else if (fullGcName.contains(name)) {
                fullGc = item;
            }
        }
    }

    @Override
    public long yongGcCollectionCount() {
        if (yongGc == null) {
            return 0;
        }
        return yongGc.getCollectionCount();
    }

    @Override
    public long yongGcCollectionTime() {
        if (yongGc == null) {
            return 0;
        }
        return yongGc.getCollectionTime();
    }

    @Override
    public long fullGcCollectionCount() {
        if (fullGc == null) {
            return 0;
        }
        return fullGc.getCollectionCount();
    }

    @Override
    public long fullGcCollectionTime() {
        if (fullGc == null) {
            return 0;
        }
        return fullGc.getCollectionTime();
    }

    @Override
    public long spanYongGcCollectionCount() {

        long current = yongGcCollectionCount();
        if (lastYoungGcCollectionCount == -1) {
            lastYoungGcCollectionCount = current;
            return 0;
        } else {
            long result = current - lastYoungGcCollectionCount;
            lastYoungGcCollectionCount = current;
            return result;
        }
    }

    @Override
    public long spanYongGcCollectionTime() {
        long current = yongGcCollectionTime();
        if (lastYoungGcCollectionTime == -1) {
            lastYoungGcCollectionTime = current;
            return 0;
        } else {
            long result = current - lastYoungGcCollectionTime;
            lastYoungGcCollectionTime = current;
            return result;
        }
    }

    @Override
    public long spanFullGcCollectionCount() {
        long current = fullGcCollectionCount();
        if (lastFullGcCollectionCount == -1) {
            lastFullGcCollectionCount = current;
            return 0;
        } else {
            long result = current - lastFullGcCollectionCount;
            lastFullGcCollectionCount = current;
            return result;
        }
    }

    @Override
    public long spanFullGcCollectionTime() {
        long current = fullGcCollectionTime();
        if (lastFullGcCollectionTime == -1) {
            lastFullGcCollectionTime = current;
            return 0;
        } else {
            long result = current - lastFullGcCollectionTime;
            lastFullGcCollectionTime = current;
            return result;
        }
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("yongGcCollectionCount", yongGcCollectionCount());
        map.put("yongGcCollectionTime", yongGcCollectionTime());
        map.put("fullGcCollectionCount", fullGcCollectionCount());
        map.put("fullGcCollectionTime", fullGcCollectionTime());
        map.put("spanYongGcCollectionCount", spanYongGcCollectionCount());
        map.put("spanYongGcCollectionTime", spanYongGcCollectionTime());
        map.put("spanFullGcCollectionCount", spanFullGcCollectionCount());
        map.put("spanFullGcCollectionTime", spanFullGcCollectionTime());
        return map;
    }
}
