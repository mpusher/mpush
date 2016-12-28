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

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.api.srd.*;
import com.mpush.tools.Jsons;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.curator.utils.ZKPaths.PATH_SEPARATOR;

/**
 * Created by ohun on 16/9/22.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ZKServiceRegistryAndDiscovery extends BaseService implements ServiceRegistry, ServiceDiscovery {

    public static final ZKServiceRegistryAndDiscovery I = new ZKServiceRegistryAndDiscovery();

    private final ZKClient client;

    public ZKServiceRegistryAndDiscovery() {
        this.client = ZKClient.I;
    }

    @Override
    public void start(Listener listener) {
        if (isRunning()) {
            listener.onSuccess();
        } else {
            super.start(listener);
        }
    }

    @Override
    public void stop(Listener listener) {
        if (isRunning()) {
            super.stop(listener);
        } else {
            listener.onSuccess();
        }
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        client.start(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        client.stop(listener);
    }

    @Override
    public void register(ServiceNode node) {
        if (node.isPersistent()) {
            client.registerPersist(node.nodePath(), Jsons.toJson(node));
        } else {
            client.registerEphemeral(node.nodePath(), Jsons.toJson(node));
        }
    }

    @Override
    public void deregister(ServiceNode node) {
        if (client.isRunning()) {
            client.remove(node.nodePath());
        }
    }

    @Override
    public List<ServiceNode> lookup(String serviceName) {
        List<String> childrenKeys = client.getChildrenKeys(serviceName);
        if (childrenKeys == null || childrenKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return childrenKeys.stream()
                .map(key -> serviceName + PATH_SEPARATOR + key)
                .map(client::get)
                .filter(Objects::nonNull)
                .map(childData -> Jsons.fromJson(childData, CommonServiceNode.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void subscribe(String watchPath, ServiceListener listener) {
        client.registerListener(new ZKCacheListener(watchPath, listener));
    }

    @Override
    public void unsubscribe(String path, ServiceListener listener) {

    }
}
