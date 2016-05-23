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

package com.mpush.tools.config;

import com.mpush.tools.MPushUtil;

import static com.mpush.tools.MPushUtil.getInetAddress;

/**
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public class ConfigManager {
    public static final ConfigManager I = new ConfigManager();

    private ConfigManager() {
    }

    public int getHeartbeat(int min, int max) {
        return Math.max(
                CC.mp.core.min_heartbeat,
                Math.min(max, CC.mp.core.max_heartbeat)
        );
    }

    public String getLocalIp() {
        return MPushUtil.getLocalIp();
    }

    public String getPublicIp() {
        String localIp = getInetAddress();

        String remoteIp = CC.mp.net.public_ip_mapping.getString(localIp);

        return remoteIp;
    }
}
