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

package com.mpush.client.user;

import com.mpush.api.spi.common.MQClientFactory;
import com.mpush.api.spi.common.MQMessageReceiver;
import com.mpush.tools.Utils;
import com.mpush.tools.config.ConfigTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mpush.api.event.Topics.OFFLINE_CHANNEL;
import static com.mpush.api.event.Topics.ONLINE_CHANNEL;

/**
 * Created by ohun on 2016/1/4.
 *
 * @author ohun@live.cn
 */
public class UserStatusChangeListener implements MQMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserStatusChangeListener.class);

    //只需要一台机器注册online、offline 消息通道
    public UserStatusChangeListener() {
        MQClientFactory.create().subscribe(ONLINE_CHANNEL, this);
        MQClientFactory.create().subscribe(OFFLINE_CHANNEL, this);
    }

    @Override
    public void receive(String channel, Object message) {

    }
}
