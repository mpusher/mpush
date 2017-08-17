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

package com.mpush.api.srd;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ohun on 2016/12/27.
 *
 * @author ohun@live.cn (夜色)
 */
public final class CommonServiceNode implements ServiceNode {

    private String host;

    private int port;

    private Map<String, Object> attrs;

    private transient String name;

    private transient String nodeId;

    private transient boolean persistent;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public void setServiceName(String name) {
        this.name = name;
    }

    public CommonServiceNode addAttr(String name, Object value) {
        if (attrs == null) attrs = new HashMap<>();
        attrs.put(name, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttr(String name) {
        if (attrs == null || attrs.isEmpty()) {
            return null;
        }
        return (T) attrs.get(name);
    }

    @Override
    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public String hostAndPort() {
        return host + ":" + port;
    }

    @Override
    public String serviceName() {
        return name;
    }

    @Override
    public String nodeId() {
        if (nodeId == null) {
            nodeId = UUID.randomUUID().toString();
        }
        return nodeId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    @Override
    public String toString() {
        return "{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", attrs=" + attrs +
                ", persistent=" + persistent +
                '}';
    }
}
