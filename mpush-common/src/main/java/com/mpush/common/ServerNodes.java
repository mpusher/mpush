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

package com.mpush.common;

import com.mpush.api.srd.CommonServiceNode;
import com.mpush.api.srd.ServiceNames;
import com.mpush.api.srd.ServiceNode;
import com.mpush.tools.Utils;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.ConfigManager;

import java.util.UUID;

import static com.mpush.api.srd.ServiceNames.ATTR_PUBLIC_IP;

/**
 * Created by ohun on 2016/12/27.
 *
 * @author ohun@live.cn (夜色)
 */
public class ServerNodes {
    public static final ServiceNode CS = cs();
    public static final ServiceNode GS = gs();


    private static ServiceNode cs() {
        CommonServiceNode cs = new CommonServiceNode();
        cs.setHost(Utils.getLocalIp());
        cs.setPort(CC.mp.net.connect_server_port);
        cs.setPersistent(false);
        cs.setName(ServiceNames.CONN_SERVER);
        cs.addAttr(ATTR_PUBLIC_IP, ConfigManager.I.getPublicIp());
        return cs;
    }

    private static ServiceNode gs() {
        CommonServiceNode cs = new CommonServiceNode();
        cs.setHost(Utils.getLocalIp());
        cs.setPort(CC.mp.net.gateway_server_port);
        cs.setPersistent(false);
        cs.setName(ServiceNames.GATEWAY_SERVER);
        return cs;
    }
}
