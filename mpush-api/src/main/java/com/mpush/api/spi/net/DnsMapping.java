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

package com.mpush.api.spi.net;


import java.net.URL;
import java.util.Objects;

public class DnsMapping {
    protected String ip;
    protected int port;

    public DnsMapping() {
    }

    public DnsMapping(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public DnsMapping setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public DnsMapping setPort(int port) {
        this.port = port;
        return this;
    }

    public static DnsMapping parse(String addr) {
        String[] host_port = Objects.requireNonNull(addr, "dns mapping can not be null")
                .split(":");
        if (host_port.length == 1) {
            return new DnsMapping(host_port[0], 80);
        } else {
            return new DnsMapping(host_port[0], Integer.valueOf(host_port[1]));
        }
    }

    public String translate(URL uri) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(uri.getProtocol()).append("://")
                .append(ip)
                .append(':')
                .append(port)
                .append(uri.getPath());
        String query = uri.getQuery();
        if (query != null) sb.append('?').append(query);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DnsMapping that = (DnsMapping) o;

        if (port != that.port) return false;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }
}
