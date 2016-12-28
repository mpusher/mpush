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

package com.mpush.zk;

import com.google.common.base.Strings;
import com.mpush.api.srd.CommonServiceNode;
import com.mpush.api.srd.ServiceListener;
import com.mpush.tools.Jsons;
import com.mpush.tools.log.Logs;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * Created by ohun on 2016/12/28.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ZKCacheListener implements TreeCacheListener {

    private final String watchPath;

    private final ServiceListener listener;

    public ZKCacheListener(String watchPath, ServiceListener listener) {
        this.watchPath = watchPath;
        this.listener = listener;
    }

    @Override
    public void childEvent(CuratorFramework curator, TreeCacheEvent event) throws Exception {
        ChildData data = event.getData();
        if (data == null) return;
        String dataPath = data.getPath();
        if (Strings.isNullOrEmpty(dataPath)) return;
        if (dataPath.startsWith(watchPath)) {
            switch (event.getType()) {
                case NODE_ADDED:
                    listener.onServiceAdded(dataPath, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
                case NODE_REMOVED:
                    listener.onServiceRemoved(dataPath, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
                case NODE_UPDATED:
                    listener.onServiceUpdated(dataPath, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
            }
            Logs.RSD.info("ZK node data change={}, nodePath={}, watchPath={}, ns={}");
        }
    }
}
