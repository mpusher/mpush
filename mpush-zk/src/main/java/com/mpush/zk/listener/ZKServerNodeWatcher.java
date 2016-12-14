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
import com.mpush.zk.cache.ZKServerNodeCache;
import com.mpush.zk.node.ZKServerNode;

import java.util.List;

public final class ZKServerNodeWatcher extends ZKNodeCacheWatcher {
    private final ZKPath path;
    private final ZKServerNodeCache cache;

    public static ZKServerNodeWatcher buildConnect() {
        return new ZKServerNodeWatcher(ZKPath.CONNECT_SERVER);
    }

    public static ZKServerNodeWatcher buildGateway() {
        return new ZKServerNodeWatcher(ZKPath.GATEWAY_SERVER);
    }

    public static ZKServerNodeWatcher build(ZKPath path, ZKServerNodeCache cache) {
        return new ZKServerNodeWatcher(path, cache);
    }

    public ZKServerNodeWatcher(ZKPath path) {
        this.path = path;
        this.cache = new ZKServerNodeCache();
    }

    public ZKServerNodeWatcher(ZKPath path, ZKServerNodeCache cache) {
        this.path = path;
        this.cache = cache;
    }

    @Override
    protected void onNodeAdded(String path, byte[] data) {
        ZKServerNode serverApp = Jsons.fromJson(data, ZKServerNode.class);
        cache.put(path, serverApp);
    }

    @Override
    protected void onNodeRemoved(String path, byte[] data) {
        cache.remove(path);
    }

    @Override
    protected void onNodeUpdated(String path, byte[] data) {
        ZKServerNode serverApp = Jsons.fromJson(data, ZKServerNode.class);
        cache.put(path, serverApp);
    }

    @Override
    public String watchPath() {
        return path.getNodePath();
    }

    @Override
    protected void beforeWatch() {
        Logs.ZK.info("start init zk server data");
        List<String> rawData = ZKClient.I.getChildrenKeys(path.getRootPath());
        for (String raw : rawData) {
            String fullPath = path.getFullPath(raw);
            ZKServerNode app = getServerNode(fullPath);
            cache.put(fullPath, app);
        }
        Logs.ZK.info("end init zk server data");
    }

    private ZKServerNode getServerNode(String fullPath) {
        String rawApp = ZKClient.I.get(fullPath);
        ZKServerNode app = Jsons.fromJson(rawApp, ZKServerNode.class);
        return app;
    }

    public ZKServerNodeCache getCache() {
        return cache;
    }

}
