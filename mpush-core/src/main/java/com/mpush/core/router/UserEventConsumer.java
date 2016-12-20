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

package com.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.mpush.api.event.UserOfflineEvent;
import com.mpush.api.event.UserOnlineEvent;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.common.user.UserManager;
import com.mpush.tools.event.EventConsumer;

import static com.mpush.api.event.Topics.OFFLINE_CHANNEL;
import static com.mpush.api.event.Topics.ONLINE_CHANNEL;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
/*package*/ final class UserEventConsumer extends EventConsumer {

    @Subscribe
    void on(UserOnlineEvent event) {
        UserManager.I.addToOnlineList(event.getUserId());
        RedisManager.I.publish(ONLINE_CHANNEL, event.getUserId());
    }

    @Subscribe
    void on(UserOfflineEvent event) {
        UserManager.I.remFormOnlineList(event.getUserId());
        RedisManager.I.publish(OFFLINE_CHANNEL, event.getUserId());
    }
}
