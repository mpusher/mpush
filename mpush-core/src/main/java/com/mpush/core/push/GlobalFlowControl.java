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

package com.mpush.core.push;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class GlobalFlowControl implements FlowControl {
    private final int limit;
    private final int maxLimit;
    private final int duration;
    private final AtomicInteger count = new AtomicInteger();
    private final AtomicInteger total = new AtomicInteger();
    private volatile long start;

    public GlobalFlowControl(int limit, int maxLimit, int duration) {
        this.limit = limit;
        this.maxLimit = maxLimit;
        this.duration = duration;
    }

    @Override
    public void reset() {
        count.set(0);
        start = System.currentTimeMillis();
    }

    @Override
    public int total() {
        return total.get();
    }

    @Override
    public boolean checkQps() {
        if (count.incrementAndGet() < limit) {
            return true;
        }

        if (total.get() > maxLimit) throw new OverFlowException();

        if (System.currentTimeMillis() - start > duration) {
            reset();
            return true;
        }
        return false;
    }

    @Override
    public int incTotal() {
        return total.get();
    }

    @Override
    public int getRemaining() {
        return duration - (int) (System.currentTimeMillis() - start);
    }

    @Override
    public String report() {
        return "total:d%, count:%d, qps:d%";
    }
}
