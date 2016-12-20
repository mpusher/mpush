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

import com.mpush.zk.node.ZKRedisNode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public class ZKRedisNodeCache implements ZKNodeCache<ZKRedisNode> {

    private List<ZKRedisNode> nodes = Collections.emptyList();

    @Override
    public void addAll(List<ZKRedisNode> list) {
        nodes = list;
    }

    @Override
    public void put(String fullPath, ZKRedisNode node) {
        throw new UnsupportedOperationException("can not put one redis node, name=" + fullPath);
    }

    @Override
    public ZKRedisNode remove(String fullPath) {
        nodes = Collections.emptyList();
        return null;
    }

    @Override
    public Collection<ZKRedisNode> values() {
        return nodes;
    }

    @Override
    public void clear() {
        nodes = Collections.emptyList();
    }

    @Override
    public int size() {
        return nodes.size();
    }
}
