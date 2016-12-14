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

package com.mpush.zk.listener;

import com.mpush.tools.Jsons;
import com.mpush.tools.log.Logs;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.zk.cache.ZKDnsNodeCache;
import com.mpush.zk.node.ZKDnsNode;

import java.util.List;

/**
 * redis 监控
 */
public class ZKDnsNodeWatcher extends ZKNodeCacheWatcher {

    private final ZKDnsNodeCache cache = new ZKDnsNodeCache();


    @Override
    protected void beforeWatch() {
        Logs.ZK.info("start init zk dns data");
        List<String> rawData = ZKClient.I.getChildrenKeys(ZKPath.DNS_MAPPING.getRootPath());
        for (String raw : rawData) {
            String fullPath = ZKPath.DNS_MAPPING.getFullPath(raw);
            cache.put(fullPath, getZKNode(fullPath));
        }
        Logs.ZK.info("end init zk dns data");
    }

    private ZKDnsNode getZKNode(String fullPath) {
        String rawData = ZKClient.I.get(fullPath);
        return Jsons.fromJson(rawData, ZKDnsNode.class);
    }

    @Override
    protected void onNodeAdded(String path, byte[] data) {
        ZKDnsNode node = Jsons.fromJson(data, ZKDnsNode.class);
        if (node != null) {
            cache.put(path, node);
        }
    }

    @Override
    protected void onNodeRemoved(String path, byte[] data) {
        cache.remove(path);
    }

    @Override
    protected void onNodeUpdated(String path, byte[] data) {
        ZKDnsNode node = Jsons.fromJson(data, ZKDnsNode.class);
        if (node != null) {
            cache.put(path, node);
        }
    }

    @Override
    public String watchPath() {
        return ZKPath.DNS_MAPPING.getRootPath();
    }

    public ZKDnsNodeCache getCache() {
        return cache;
    }
}
