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

    protected final Map<String, ZKServerNode> cache = Maps.newConcurrentMap();

    @Override
    public void addAll(List<ZKServerNode> list) {

    }

    @Override
    public void put(String fullPath, ZKServerNode node) {
        if (StringUtils.isNotBlank(fullPath) && node != null) {
            cache.put(fullPath, node);
        } else {
            logger.error("fullPath is null or application is null");
        }
        printList();
    }

    @Override
    public ZKServerNode remove(String fullPath) {
        ZKServerNode node = cache.remove(fullPath);
        printList();
        return node;
    }

    @Override
    public Collection<ZKServerNode> values() {
        return Collections.unmodifiableCollection(cache.values());
    }

    @Override
    public void clear() {
        cache.clear();
    }

    private void printList() {
        for (ZKServerNode app : cache.values()) {
            logger.warn(app.toString());
        }
    }
}
