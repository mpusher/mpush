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

package com.mpush.zk.node;

import com.mpush.api.spi.net.DnsMapping;
import com.mpush.zk.ZKPath;

/**
 * Created by ohun on 16/9/22.
 *
 * @author ohun@live.cn (夜色)
 */
public class ZKDnsNode extends DnsMapping implements ZKNode {
    private String origin;

    public ZKDnsNode() {
    }

    public ZKDnsNode(String origin, String ip, int port) {
        super(ip, port);
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

    public ZKDnsNode setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZKDnsNode node = (ZKDnsNode) o;

        if (port != node.port) return false;
        return ip.equals(node.ip);

    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "ZKDnsNode{" +
                "origin='" + origin + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
