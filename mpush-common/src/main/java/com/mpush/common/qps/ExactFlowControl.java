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

import com.mpush.tools.common.RollingNumber;

import java.util.concurrent.TimeUnit;

import static com.mpush.tools.common.RollingNumber.Event.SUCCESS;

/**
 * Created by ohun on 2016/12/23.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ExactFlowControl implements FlowControl {
    private static final long DELAY_100_MS = TimeUnit.MILLISECONDS.toNanos(1);
    private final RollingNumber rollingNumber;
    private final int qps_pre_10_mills;
    private final long start0 = System.nanoTime();

    public ExactFlowControl(int qps) {
        int timeInMilliseconds = 1000;// 1s
        int numberOfBuckets = 100;//把1s 分成 100份，10ms一份， 要计算处 每10ms内允许的最大数量qps_pre_10_mills

        int _10_mills = timeInMilliseconds / numberOfBuckets;//=10

        double real_qps_pre_10_mills = (qps / 1000f) * _10_mills;

        if (real_qps_pre_10_mills < 1) {//qps < 100;
            numberOfBuckets = 1;
            real_qps_pre_10_mills = qps;
        }

        this.qps_pre_10_mills = (int) real_qps_pre_10_mills;
        this.rollingNumber = new RollingNumber(timeInMilliseconds, numberOfBuckets);
    }

    @Override
    public void reset() {

    }

    @Override
    public int total() {
        return (int) rollingNumber.getCumulativeSum(SUCCESS);
    }

    @Override
    public boolean checkQps() throws OverFlowException {
        if (rollingNumber.getValueOfLatestBucket(SUCCESS) < qps_pre_10_mills) {
            rollingNumber.increment(SUCCESS);
            return true;
        }
        return false;
    }

    @Override
    public long getDelay() {
        return DELAY_100_MS;
    }

    @Override
    public int qps() {
        return (int) rollingNumber.getRollingSum(SUCCESS);
    }

    @Override
    public String report() {
        return String.format("total:%d, count:%d, qps:%d, avg_qps:%d",
                total(), rollingNumber.getValueOfLatestBucket(SUCCESS), qps(),
                TimeUnit.SECONDS.toNanos(total()) / (System.nanoTime() - start0));
    }
}
