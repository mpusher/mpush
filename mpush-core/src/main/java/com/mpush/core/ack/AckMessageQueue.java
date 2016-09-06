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

package com.mpush.core.ack;

import com.mpush.api.protocol.Packet;
import com.mpush.api.push.PushException;
import com.mpush.common.ErrorCode;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.tools.log.Logs;
import com.mpush.tools.thread.PoolThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static com.mpush.api.protocol.Command.ERROR;
import static com.mpush.api.protocol.Command.OK;
import static com.mpush.tools.thread.ThreadNames.T_ARK_REQ_TIMER;

/**
 * Created by ohun on 16/9/5.
 *
 * @author ohun@live.cn (夜色)
 */
public final class AckMessageQueue {
    private final Logger logger = LoggerFactory.getLogger(AckMessageQueue.class);
    public static final AckMessageQueue I = new AckMessageQueue();
    private final ConcurrentMap<Integer, AckContext> queue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutor;

    private AckMessageQueue() {
        scheduledExecutor = new ScheduledThreadPoolExecutor(1, new PoolThreadFactory(T_ARK_REQ_TIMER), (r, e) -> {
            logger.error("one ack context was rejected, context=" + r);
        });
    }

    public void put(int sessionId, AckContext context) {
        queue.put(sessionId, context);
        context.pushMessageId = sessionId;
        scheduledExecutor.schedule(context, 3, TimeUnit.SECONDS);
    }

    public AckContext getAndRemove(int sessionId) {
        return queue.remove(sessionId);
    }

}
