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

/**
 * Created by ohun on 16/10/24.
 *
 * 流量控制
 *
 * @author ohun@live.cn (夜色)
 */
public interface FlowControl {

    void reset();

    int total();

    /**
     * 判断瞬时qps是否超出设定的流量
     *
     * @return true/false
     * @throws OverFlowException 超出最大限制，会直接抛出异常
     */
    boolean checkQps() throws OverFlowException;

    default void end(Object results) {
    }

    /**
     * 超出流控的任务，应该延迟执行的时间(ns)
     *
     * @return 单位纳秒
     */
    long getDelay();

    /**
     * 任务从开始到现在的平均qps
     *
     * @return 平均qps
     */
    int qps();

    String report();

}
