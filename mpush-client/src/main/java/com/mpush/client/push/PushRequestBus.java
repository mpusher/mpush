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

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.client.MPushClient;
import com.mpush.monitor.service.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public class PushRequestBus extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(PushRequestBus.class);
    private final Map<Integer, PushRequest> reqQueue = new ConcurrentHashMap<>(1024);
    private ScheduledExecutorService scheduledExecutor;
    private final MPushClient mPushClient;

    public PushRequestBus(MPushClient mPushClient) {
        this.mPushClient = mPushClient;
    }

    public Future<?> put(int sessionId, PushRequest request) {
        reqQueue.put(sessionId, request);
        return scheduledExecutor.schedule(request, request.getTimeout(), TimeUnit.MILLISECONDS);
    }

    public PushRequest getAndRemove(int sessionId) {
        return reqQueue.remove(sessionId);
    }

    public void asyncCall(Runnable runnable) {
        scheduledExecutor.execute(runnable);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        scheduledExecutor = mPushClient.getThreadPoolManager().getPushClientTimer();
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        listener.onSuccess();
    }
}
