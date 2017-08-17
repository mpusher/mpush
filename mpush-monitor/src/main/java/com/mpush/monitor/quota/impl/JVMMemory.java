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
import com.mpush.monitor.quota.MemoryQuota;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.Map;

public class JVMMemory implements MemoryQuota {

    private final List<String> permGenName = Lists.newArrayList("CMS Perm Gen", "Perm Gen", "PS Perm Gen", "G1 Perm Gen");

    private final List<String> oldGenName = Lists.newArrayList("CMS Old Gen", "Tenured Gen", "PS Old Gen", "G1 Old Gen");

    private final List<String> edenSpaceName = Lists.newArrayList("Par Eden Space", "Eden Space", "PS Eden Space", "G1 Eden");

    private final List<String> psSurvivorName = Lists.newArrayList("Par Survivor Space", "Survivor Space", "PS Survivor Space", "G1 Survivor");

    public static final JVMMemory I = new JVMMemory();

    private MemoryMXBean memoryMXBean;

    private MemoryPoolMXBean permGenMxBean;
    private MemoryPoolMXBean oldGenMxBean;
    private MemoryPoolMXBean edenSpaceMxBean;
    private MemoryPoolMXBean pSSurvivorSpaceMxBean;

    public JVMMemory() {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean item : list) {
            String name = item.getName();
            if (permGenName.contains(name)) {
                permGenMxBean = item;
            } else if (oldGenName.contains(name)) {
                oldGenMxBean = item;
            } else if (edenSpaceName.contains(name)) {
                edenSpaceMxBean = item;
            } else if (psSurvivorName.contains(name)) {
                pSSurvivorSpaceMxBean = item;
            }
        }
    }

    @Override
    public long heapMemoryCommitted() {
        return memoryMXBean.getHeapMemoryUsage().getCommitted();
    }

    @Override
    public long heapMemoryInit() {
        return memoryMXBean.getHeapMemoryUsage().getInit();
    }

    @Override
    public long heapMemoryMax() {
        return memoryMXBean.getHeapMemoryUsage().getMax();
    }

    @Override
    public long heapMemoryUsed() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    @Override
    public long nonHeapMemoryCommitted() {
        return memoryMXBean.getNonHeapMemoryUsage().getCommitted();
    }

    @Override
    public long nonHeapMemoryInit() {
        return memoryMXBean.getNonHeapMemoryUsage().getInit();
    }

    @Override
    public long nonHeapMemoryMax() {
        return memoryMXBean.getNonHeapMemoryUsage().getMax();
    }

    @Override
    public long nonHeapMemoryUsed() {
        return memoryMXBean.getNonHeapMemoryUsage().getUsed();
    }

    @Override
    public long permGenCommitted() {
        if (permGenMxBean == null) {
            return 0;
        }
        return permGenMxBean.getUsage().getCommitted();
    }

    @Override
    public long permGenInit() {
        if (permGenMxBean == null) {
            return 0;
        }
        return permGenMxBean.getUsage().getInit();
    }

    @Override
    public long permGenMax() {
        if (permGenMxBean == null) {
            return 0;
        }
        return permGenMxBean.getUsage().getMax();
    }

    @Override
    public long permGenUsed() {
        if (permGenMxBean == null) {
            return 0;
        }
        return permGenMxBean.getUsage().getUsed();
    }

    @Override
    public long oldGenCommitted() {
        if (oldGenMxBean == null) {
            return 0;
        }
        return oldGenMxBean.getUsage().getCommitted();
    }

    @Override
    public long oldGenInit() {
        if (oldGenMxBean == null) {
            return 0;
        }
        return oldGenMxBean.getUsage().getInit();
    }

    @Override
    public long oldGenMax() {
        if (oldGenMxBean == null) {
            return 0;
        }
        return oldGenMxBean.getUsage().getMax();
    }

    @Override
    public long oldGenUsed() {
        if (oldGenMxBean == null) {
            return 0;
        }
        return oldGenMxBean.getUsage().getUsed();
    }

    @Override
    public long edenSpaceCommitted() {
        if (null == edenSpaceMxBean) {
            return 0;
        }
        return edenSpaceMxBean.getUsage().getCommitted();
    }

    @Override
    public long edenSpaceInit() {
        if (null == edenSpaceMxBean) {
            return 0;
        }
        return edenSpaceMxBean.getUsage().getInit();
    }

    @Override
    public long edenSpaceMax() {
        if (null == edenSpaceMxBean) {
            return 0;
        }
        return edenSpaceMxBean.getUsage().getMax();
    }

    @Override
    public long edenSpaceUsed() {
        if (null == edenSpaceMxBean) {
            return 0;
        }
        return edenSpaceMxBean.getUsage().getUsed();
    }

    @Override
    public long survivorCommitted() {
        if (null == pSSurvivorSpaceMxBean) {
            return 0;
        }
        return pSSurvivorSpaceMxBean.getUsage().getCommitted();
    }

    @Override
    public long survivorInit() {
        if (null == pSSurvivorSpaceMxBean) {
            return 0;
        }
        return pSSurvivorSpaceMxBean.getUsage().getInit();
    }

    @Override
    public long survivorMax() {
        if (null == pSSurvivorSpaceMxBean) {
            return 0;
        }
        return pSSurvivorSpaceMxBean.getUsage().getMax();
    }

    @Override
    public long survivorUsed() {
        if (null == pSSurvivorSpaceMxBean) {
            return 0;
        }
        return pSSurvivorSpaceMxBean.getUsage().getUsed();
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("heapMemoryCommitted", heapMemoryCommitted());
        map.put("heapMemoryInit", heapMemoryInit());
        map.put("heapMemoryMax", heapMemoryMax());
        map.put("heapMemoryUsed", heapMemoryUsed());
        map.put("nonHeapMemoryCommitted", nonHeapMemoryCommitted());
        map.put("nonHeapMemoryInit", nonHeapMemoryInit());
        map.put("nonHeapMemoryMax", nonHeapMemoryMax());
        map.put("nonHeapMemoryUsed", nonHeapMemoryUsed());
        map.put("permGenCommitted", permGenCommitted());
        map.put("permGenInit", permGenInit());
        map.put("permGenMax", permGenMax());
        map.put("permGenUsed", permGenUsed());
        map.put("oldGenCommitted", oldGenCommitted());
        map.put("oldGenInit", oldGenInit());
        map.put("oldGenMax", oldGenMax());
        map.put("oldGenUsed", oldGenUsed());
        map.put("edenSpaceCommitted", edenSpaceCommitted());
        map.put("edenSpaceInit", edenSpaceInit());
        map.put("edenSpaceMax", edenSpaceMax());
        map.put("edenSpaceUsed", edenSpaceUsed());
        map.put("survivorCommitted", survivorCommitted());
        map.put("survivorInit", survivorInit());
        map.put("survivorMax", survivorMax());
        map.put("survivorUsed", survivorUsed());
        return map;
    }
}
