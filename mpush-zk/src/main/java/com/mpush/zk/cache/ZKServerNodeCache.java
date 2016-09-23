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
import com.mpush.zk.node.ZKServerNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class ZKServerNodeCache implements ZKNodeCache<ZKServerNode> {

    private final Logger logger = LoggerFactory.getLogger(ZKServerNodeCache.class);

    protected final Map<String, ZKServerNode> nodes = Maps.newConcurrentMap();

    @Override
    public void addAll(List<ZKServerNode> list) {

    }

    @Override
    public void put(String fullPath, ZKServerNode node) {
        if (StringUtils.isNotBlank(fullPath) && node != null) {
            nodes.put(fullPath, node);
        } else {
            logger.error("fullPath is null or application is null");
        }
        printCache();
    }

    @Override
    public ZKServerNode remove(String fullPath) {
        ZKServerNode node = nodes.remove(fullPath);
        printCache();
        return node;
    }

    @Override
    public Collection<ZKServerNode> values() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    private void printCache() {
        for (ZKServerNode app : nodes.values()) {
            logger.warn(app.toString());
        }
    }
}
