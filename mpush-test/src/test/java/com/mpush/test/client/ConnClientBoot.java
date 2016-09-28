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

package com.mpush.test.client;

import com.google.common.collect.Lists;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKServerNodeWatcher;
import com.mpush.zk.node.ZKServerNode;

import java.util.List;

public final class ConnClientBoot extends BaseService {
    private final ZKServerNodeWatcher watcher = ZKServerNodeWatcher.buildConnect();

    public List<ZKServerNode> getServers() {
        return Lists.newArrayList(watcher.getCache().values());
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        ZKClient.I.start(listener);
        RedisManager.I.init();
        watcher.watch();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        ZKClient.I.stop();
        RedisManager.I.destroy();
    }
}