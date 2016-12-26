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

package com.mpush.core.mq;

import com.mpush.api.common.Condition;
import com.mpush.api.spi.push.IPushMessage;

/**
 * Created by ohun on 2016/12/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class MQPushMessage implements IPushMessage {

    @Override
    public boolean isBroadcast() {
        return false;
    }

    @Override
    public String getUserId() {
        return null;
    }

    @Override
    public int getClientType() {
        return 0;
    }

    @Override
    public byte[] getContent() {
        return new byte[0];
    }

    @Override
    public boolean isNeedAck() {
        return false;
    }

    @Override
    public byte getFlags() {
        return 0;
    }

    @Override
    public int getTimeoutMills() {
        return 0;
    }

    @Override
    public String getTaskId() {
        return null;
    }

    @Override
    public Condition getCondition() {
        return null;
    }
}
