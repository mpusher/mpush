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

package com.mpush.monitor.data;

import com.mpush.monitor.quota.impl.*;

/**
 * Created by yxx on 2016/5/19.
 *
 * @author ohun@live.cn
 */
public class ResultCollector {

    public MonitorResult collect() {
        MonitorResult result = new MonitorResult();
        result.addResult("jvm-info", JVMInfo.I.monitor());
        result.addResult("jvm-gc", JVMGC.I.monitor());
        result.addResult("jvm-memory", JVMMemory.I.monitor());
        result.addResult("jvm-thread", JVMThread.I.monitor());
        result.addResult("jvm-thread-pool", JVMThreadPool.I.monitor());
        return result;
    }

}
