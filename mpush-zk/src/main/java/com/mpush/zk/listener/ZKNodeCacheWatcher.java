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

import com.google.common.base.Strings;
import com.mpush.tools.log.Logs;
import com.mpush.zk.ZKClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * 缓存节点变化监听
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public abstract class ZKNodeCacheWatcher implements TreeCacheListener {

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        ChildData data = event.getData();
        if (data == null) return;
        String path = data.getPath();
        if (Strings.isNullOrEmpty(path)) return;
        if (path.startsWith(watchPath())) {
            switch (event.getType()) {
                case NODE_ADDED:
                    onNodeAdded(path, data.getData());
                    break;
                case NODE_REMOVED:
                    onNodeRemoved(path, data.getData());
                    break;
                case NODE_UPDATED:
                    onNodeUpdated(path, data.getData());
                    break;
            }
            Logs.ZK.info("ZK node data change={}, nodePath={}, watchPath={}, ns={}", event.getType(), path, watchPath(), client.getNamespace());
        }
    }

    public ZKNodeCacheWatcher watch() {
        beforeWatch();
        ZKClient.I.registerListener(this);
        return this;
    }

    @Deprecated
    public final void beginWatch() {
        watch();
    }

    public abstract String watchPath();

    protected void beforeWatch() {

    }

    protected void onNodeAdded(String path, byte[] data) {

    }

    protected void onNodeRemoved(String path, byte[] data) {

    }

    protected void onNodeUpdated(String path, byte[] data) {

    }
}
