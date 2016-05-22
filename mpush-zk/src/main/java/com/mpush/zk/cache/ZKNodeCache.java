package com.mpush.zk.cache;

import com.mpush.zk.node.ZKNode;

import java.util.Collection;
import java.util.List;

public interface ZKNodeCache<T extends ZKNode> {

    void addAll(List<T> list);

    void put(String fullPath, T node);

    T remove(String fullPath);

    Collection<T> values();

    void clear();

}
