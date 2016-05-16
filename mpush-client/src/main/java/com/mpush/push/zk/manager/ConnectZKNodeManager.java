package com.mpush.push.zk.manager;

import com.google.common.collect.Maps;
import com.mpush.zk.ZKServerNode;
import com.mpush.zk.ZKNodeManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ConnectZKNodeManager implements ZKNodeManager<ZKServerNode> {

    private final Logger log = LoggerFactory.getLogger(ConnectZKNodeManager.class);

    private final Map<String, ZKServerNode> cache = Maps.newConcurrentMap();

    @Override
    public void addOrUpdate(String fullPath, ZKServerNode node) {
        if (StringUtils.isNotBlank(fullPath) && node != null) {
            cache.put(fullPath, node);
        } else {
            log.error("fullPath is null or application is null");
        }
        printList();
    }

    @Override
    public void remove(String fullPath) {
        cache.remove(fullPath);
        printList();
    }

    @Override
    public Collection<ZKServerNode> getList() {
        return Collections.unmodifiableCollection(cache.values());
    }

    private void printList() {
        for (ZKServerNode app : cache.values()) {
            log.warn(app.toString());
        }
    }

}
