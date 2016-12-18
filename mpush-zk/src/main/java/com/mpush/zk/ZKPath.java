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

package com.mpush.zk;


import org.apache.curator.utils.ZKPaths;

import static org.apache.curator.utils.ZKPaths.PATH_SEPARATOR;

public enum ZKPath {
    REDIS_SERVER("/redis", "machine", "redis注册的地方"),
    CONNECT_SERVER("/cs/hosts", "machine", "connection server服务器应用注册的路径"),
    GATEWAY_SERVER("/gs/hosts", "machine", "gateway server服务器应用注册的路径"),
    WS_SERVER("/ws/hosts", "machine", "websocket server服务器应用注册的路径"),
    DNS_MAPPING("/dns/mapping", "machine", "dns mapping服务器应用注册的路径");

    ZKPath(String root, String name, String desc) {
        this.root = root;
        this.name = name;
    }

    private final String root;
    private final String name;

    public String getRootPath() {
        return root;
    }

    public String getNodePath() {
        return root + PATH_SEPARATOR + name;
    }

    public String getNodePath(String... tails) {
        String path = getNodePath();
        for (String tail : tails) {
            path += (PATH_SEPARATOR + tail);
        }
        return path;
    }

    //根据从zk中获取的app的值，拼装全路径
    public String getFullPath(String childPaths) {
        return root + PATH_SEPARATOR + childPaths;
    }

    public String getTail(String childPaths) {
        return ZKPaths.getNodeFromPath(childPaths);
    }

}
