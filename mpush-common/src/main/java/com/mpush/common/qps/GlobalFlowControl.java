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

package com.mpush.common.qps;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class GlobalFlowControl implements FlowControl {
    private final int limit;
    private final int maxLimit;
    private final long duration;
    private final AtomicInteger count = new AtomicInteger();
    private final AtomicInteger total = new AtomicInteger();
    private final long start0 = System.nanoTime();
    private volatile long start;

    public GlobalFlowControl(int qps) {
        this(qps, Integer.MAX_VALUE, 1000);
    }

    public GlobalFlowControl(int limit, int maxLimit, int duration) {
        this.limit = limit;
        this.maxLimit = maxLimit;
        this.duration = TimeUnit.MILLISECONDS.toNanos(duration);
    }

    @Override
    public void reset() {
        count.set(0);
        start = System.nanoTime();
    }

    @Override
    public int total() {
        return total.get();
    }

    @Override
    public boolean checkQps() {
        if (count.incrementAndGet() < limit) {
            total.incrementAndGet();
            return true;
        }

        if (maxLimit > 0 && total.get() > maxLimit) throw new OverFlowException(true);

        if (System.nanoTime() - start > duration) {
            reset();
            total.incrementAndGet();
            return true;
        }
        return false;
    }

    @Override
    public long getDelay() {
        return duration - (System.nanoTime() - start);
    }

    @Override
    public int qps() {
        return (int) (TimeUnit.SECONDS.toNanos(total.get()) / (System.nanoTime() - start0));
    }

    @Override
    public String report() {
        return String.format("total:%d, count:%d, qps:%d", total.get(), count.get(), qps());
    }
}
