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
}
