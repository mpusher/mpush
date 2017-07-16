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

import com.mpush.tools.Utils;
import com.mpush.tools.config.CC.mp.net.public_ip_mapping;

/**
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public final class ConfigTools {

    private ConfigTools() {
    }

    public static int getHeartbeat(int min, int max) {
        return Math.max(
                CC.mp.core.min_heartbeat,
                Math.min(max, CC.mp.core.max_heartbeat)
        );
    }

    /**
     * 获取内网IP地址
     *
     * @return 内网IP地址
     */
    public static String getLocalIp() {
        if (CC.mp.net.local_ip.length() > 0) {
            return CC.mp.net.local_ip;
        }
        return Utils.lookupLocalIp();
    }

    /**
     * 获取外网IP地址
     *
     * @return 外网IP地址
     */
    public static String getPublicIp() {

        if (CC.mp.net.public_ip.length() > 0) {
            return CC.mp.net.public_ip;
        }

        String localIp = getLocalIp();

        String remoteIp = public_ip_mapping.getString(localIp);

        if (remoteIp == null) {
            remoteIp = Utils.lookupExtranetIp();
        }

        return remoteIp == null ? localIp : remoteIp;
    }


    public static String getConnectServerRegisterIp() {
        if (CC.mp.net.connect_server_register_ip.length() > 0) {
            return CC.mp.net.connect_server_register_ip;
        }
        return getPublicIp();
    }

    public static String getGatewayServerRegisterIp() {
        if (CC.mp.net.gateway_server_register_ip.length() > 0) {
            return CC.mp.net.gateway_server_register_ip;
        }
        return getLocalIp();
    }
}
