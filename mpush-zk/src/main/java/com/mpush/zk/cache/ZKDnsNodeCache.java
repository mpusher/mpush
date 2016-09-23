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

package com.mpush.zk.cache;

import com.google.common.collect.Maps;
import com.mpush.zk.node.ZKDnsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class ZKDnsNodeCache implements ZKNodeCache<ZKDnsNode> {

    private final Logger logger = LoggerFactory.getLogger(ZKDnsNodeCache.class);

    protected final Map<String, ZKDnsNode> nodes = Maps.newConcurrentMap();

    protected final Map<String, List<ZKDnsNode>> mappings = Maps.newConcurrentMap();

    @Override
    public void addAll(List<ZKDnsNode> list) {
    }

    @Override
    public void put(String path, ZKDnsNode node) {
        nodes.put(path, node);
        List<ZKDnsNode> nodes = mappings.get(node.getOrigin());
        if (nodes == null) {
            nodes = new ArrayList<>();
            mappings.put(node.getOrigin(), nodes);
        }
        nodes.add(node);
        printCache();
    }

    @Override
    public ZKDnsNode remove(String path) {
        ZKDnsNode node = nodes.remove(path);
        if (node != null) {
            List<ZKDnsNode> nodes = mappings.get(node.getOrigin());
            if (nodes != null) {
                nodes.remove(node);
            }
            printCache();
        }
        return node;
    }

    @Override
    public Collection<ZKDnsNode> values() {
        return nodes.values();
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    public List<ZKDnsNode> get(String origin) {
        return mappings.get(origin);
    }

    private void printCache() {
        for (ZKDnsNode node : nodes.values()) {
            logger.warn(node.toString());
        }
    }
}
