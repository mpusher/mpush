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

package com.mpush.zk.node;


import com.mpush.tools.Utils;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.ConfigManager;
import com.mpush.zk.ZKPath;

/**
 * MPUSH server 节点配置
 * <p>
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public class ZKServerNode implements ZKNode {

    public static final ZKServerNode CS_NODE = csNode();
    public static final ZKServerNode GS_NODE = gsNode();

    private String ip;

    private int port;

    private String extranetIp;

    private transient String zkPath;

    public ZKServerNode() {
    }

    public ZKServerNode(String ip, int port, String extranetIp, String zkPath) {
        this.ip = ip;
        this.port = port;
        this.extranetIp = extranetIp;
        this.zkPath = zkPath;
    }

    private static ZKServerNode csNode() {
        return new ZKServerNode(Utils.getLocalIp(),
                CC.mp.net.connect_server_port,
                ConfigManager.I.getPublicIp(),
                ZKPath.CONNECT_SERVER.getNodePath());
    }

    private static ZKServerNode gsNode() {
        return new ZKServerNode(Utils.getLocalIp(),
                CC.mp.net.gateway_server_port,
                null,
                ZKPath.GATEWAY_SERVER.getNodePath());
    }

    public String getIp() {
        return ip;
    }

    public ZKServerNode setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ZKServerNode setPort(int port) {
        this.port = port;
        return this;
    }

    public String getExtranetIp() {
        return extranetIp;
    }

    public ZKServerNode setExtranetIp(String extranetIp) {
        this.extranetIp = extranetIp;
        return this;
    }

    @Override
    public String getNodePath() {
        return zkPath;
    }

    public ZKServerNode setZkPath(String zkPath) {
        this.zkPath = zkPath;
        return this;
    }

    public String getHostAndPort() {
        return ip + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZKServerNode that = (ZKServerNode) o;

        if (port != that.port) return false;
        return ip != null ? ip.equals(that.ip) : that.ip == null;

    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "ZKServerNode{" +
                "host='" + ip + '\'' +
                ", port=" + port +
                ", extranetIp='" + extranetIp + '\'' +
                ", zkPath='" + zkPath + '\'' +
                '}';
    }
}
