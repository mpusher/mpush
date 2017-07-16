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

package com.mpush.common.push;

import com.mpush.api.push.BroadcastController;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.api.spi.common.CacheManagerFactory;
import com.mpush.common.CacheKeys;

import java.util.List;

/**
 * Created by ohun on 16/10/25.
 *
 * @author ohun@live.cn (夜色)
 */
public final class RedisBroadcastController implements BroadcastController {
    private static final String TASK_DONE_FIELD = "d";
    private static final String TASK_SEND_COUNT_FIELD = "sc";
    private static final String TASK_CANCEL_FIELD = "c";
    private static final String TASK_QPS_FIELD = "q";
    private static final String TASK_SUCCESS_USER_ID = "sui";

    private static CacheManager cacheManager = CacheManagerFactory.create();

    private final String taskId;
    private final String taskKey;
    private final String taskSuccessUIDKey;

    public RedisBroadcastController(String taskId) {
        this.taskId = taskId;
        this.taskKey = CacheKeys.getPushTaskKey(taskId);
        this.taskSuccessUIDKey = taskId + ':' + TASK_SUCCESS_USER_ID;
    }

    @Override
    public String taskId() {
        return taskId;
    }

    @Override
    public int qps() {
        Integer count = cacheManager.hget(taskKey, TASK_QPS_FIELD, Integer.TYPE);
        return count == null ? 1000 : count;
    }

    @Override
    public void updateQps(int qps) {
        cacheManager.hset(taskKey, TASK_QPS_FIELD, qps);
    }

    @Override
    public boolean isDone() {
        return Boolean.TRUE.equals(cacheManager.hget(taskKey, TASK_DONE_FIELD, Boolean.class));
    }

    @Override
    public int sendCount() {
        Integer count = cacheManager.hget(taskKey, TASK_SEND_COUNT_FIELD, Integer.TYPE);
        return count == null ? 0 : count;
    }

    @Override
    public void cancel() {
        cacheManager.hset(taskKey, TASK_CANCEL_FIELD, 1);
    }

    @Override
    public boolean isCancelled() {
        Integer status = cacheManager.hget(taskKey, TASK_CANCEL_FIELD, Integer.TYPE);
        return status != null && status == 1;
    }

    @Override
    public int incSendCount(int count) {
        return (int) cacheManager.hincrBy(taskKey, TASK_SEND_COUNT_FIELD, count);
    }

    public void success(String... userIds) {
        cacheManager.lpush(taskSuccessUIDKey, userIds);
    }

    public List<String> successUserIds() {
        return cacheManager.lrange(taskSuccessUIDKey, 0, -1, String.class);
    }
}
