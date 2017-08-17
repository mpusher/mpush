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

import com.mpush.api.push.BroadcastController;
import com.mpush.common.push.RedisBroadcastController;

import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 16/10/25.
 *
 * @author ohun@live.cn (夜色)
 */
public final class RedisFlowControl implements FlowControl {

    private final BroadcastController controller;
    private final long start0 = System.nanoTime();
    private final long duration = TimeUnit.SECONDS.toNanos(1);
    private final int maxLimit;
    private int limit;
    private int count;
    private int total;
    private long start;

    public RedisFlowControl(String taskId) {
        this(taskId, Integer.MAX_VALUE);
    }

    public RedisFlowControl(String taskId, int maxLimit) {
        this.controller = new RedisBroadcastController(taskId);
        this.limit = controller.qps();
        this.maxLimit = maxLimit;
    }

    @Override
    public void reset() {
        count = 0;
        start = System.nanoTime();
    }

    @Override
    public int total() {
        return total;
    }

    @Override
    public boolean checkQps() throws OverFlowException {
        if (count < limit) {
            count++;
            total++;
            return true;
        }

        if (total() > maxLimit) {
            throw new OverFlowException(true);
        }

        if (System.nanoTime() - start > duration) {
            reset();
            total++;
            return true;
        }

        if (controller.isCancelled()) {
            throw new OverFlowException(true);
        } else {
            limit = controller.qps();
        }
        return false;
    }

    @Override
    public void end(Object result) {
        int t = total;
        if (total > 0) {
            total = 0;
            controller.incSendCount(t);
        }

        if (result != null && (result instanceof String[])) {
            controller.success((String[]) result);
        }
    }

    @Override
    public long getDelay() {
        return duration - (System.nanoTime() - start);
    }

    @Override
    public String report() {
        return String.format("total:%d, count:%d, qps:%d", total, count, qps());
    }

    @Override
    public int qps() {
        return (int) (TimeUnit.SECONDS.toNanos(total) / (System.nanoTime() - start0));
    }

    public BroadcastController getController() {
        return controller;
    }
}
