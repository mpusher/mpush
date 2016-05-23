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

package com.mpush.client.push;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public class PushRequestBus implements Runnable {
	
    public static final PushRequestBus INSTANCE = new PushRequestBus();
    private Map<Integer, PushRequest> requests = new ConcurrentHashMapV8<>(1024);
    private Executor executor = Executors.newFixedThreadPool(5);//test
    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();//test

    private PushRequestBus() {
        scheduledExecutor.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
    }

    public void put(int sessionId, PushRequest request) {
        requests.put(sessionId, request);
    }

    public PushRequest remove(int sessionId) {
        return requests.remove(sessionId);
    }

    public Executor getExecutor() {
        return executor;
    }

    @Override
    public void run() {
        if (requests.isEmpty()) return;
        Iterator<PushRequest> it = requests.values().iterator();
        while (it.hasNext()) {
            PushRequest request = it.next();
            if (request.isTimeout()) {
                it.remove();//清除超时的请求
                request.timeout();
            }
        }
    }
}
