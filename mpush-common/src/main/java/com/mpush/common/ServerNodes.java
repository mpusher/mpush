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
import com.mpush.tools.config.CC;
import com.mpush.tools.config.ConfigTools;

import static com.mpush.api.srd.ServiceNames.ATTR_PUBLIC_IP;

/**
 * Created by ohun on 2016/12/27.
 *
 * @author ohun@live.cn (夜色)
 */
public class ServerNodes {

    public static ServiceNode cs() {
        CommonServiceNode node = new CommonServiceNode();
        node.setHost(ConfigTools.getConnectServerRegisterIp());
        node.setPort(CC.mp.net.connect_server_port);
        node.setPersistent(false);
        node.setServiceName(ServiceNames.CONN_SERVER);
        node.setAttrs(CC.mp.net.connect_server_register_attr);
        return node;
    }

    public static ServiceNode ws() {
        CommonServiceNode node = new CommonServiceNode();
        node.setHost(ConfigTools.getConnectServerRegisterIp());
        node.setPort(CC.mp.net.ws_server_port);
        node.setPersistent(false);
        node.setServiceName(ServiceNames.WS_SERVER);
        //node.addAttr(ATTR_PUBLIC_IP, ConfigTools.getPublicIp());
        return node;
    }

    public static ServiceNode gs() {
        CommonServiceNode node = new CommonServiceNode();
        node.setHost(ConfigTools.getGatewayServerRegisterIp());
        node.setPort(CC.mp.net.gateway_server_port);
        node.setPersistent(false);
        node.setServiceName(ServiceNames.GATEWAY_SERVER);
        return node;
    }
}
