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

import com.mpush.api.push.PushException;
import com.mpush.tools.thread.PoolThreadFactory;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

import static com.mpush.tools.thread.ThreadNames.T_PUSH_REQ_TIMER;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public class PushRequestBus {
    public static final PushRequestBus I = new PushRequestBus();
    private final Logger logger = LoggerFactory.getLogger(PushRequestBus.class);
    private final Map<Integer, PushRequest> reqQueue = new ConcurrentHashMap<>(1024);
    private final Executor executor = ThreadPoolManager.I.getPushCallbackExecutor();
    private final ScheduledExecutorService scheduledExecutor;

    private PushRequestBus() {
        scheduledExecutor = new ScheduledThreadPoolExecutor(1, new PoolThreadFactory(T_PUSH_REQ_TIMER), (r, e) -> {
            logger.error("one push request was rejected, request=" + r);
            throw new PushException("one push request was rejected. request=" + r);
        });
    }

    public Future<?> put(int sessionId, PushRequest request) {
        reqQueue.put(sessionId, request);
        return scheduledExecutor.schedule(request, request.getTimeout(), TimeUnit.MILLISECONDS);
    }

    public PushRequest getAndRemove(int sessionId) {
        return reqQueue.remove(sessionId);
    }

    public void asyncCall(Runnable runnable) {
        executor.execute(runnable);
    }
}
