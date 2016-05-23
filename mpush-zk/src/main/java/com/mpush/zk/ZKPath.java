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

public enum ZKPath {
    REDIS_SERVER("/redis", "machine", "redis注册的地方"),
    CONNECT_SERVER("/cs/hosts", "machine", "connection server服务器应用注册的路径"),
    PUSH_SERVER("/ps/hosts", "machine", "push server服务器应用注册的路径"),
    GATEWAY_SERVER("/gs/hosts", "machine", "gateway server服务器应用注册的路径");

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
        return root + ZKPaths.PATH_SEPARATOR + name;
    }

    //根据从zk中获取的app的值，拼装全路径
    public String getFullPath(String tail) {
        return root + ZKPaths.PATH_SEPARATOR + tail;
    }

}
