package com.mpush.zk.listener;

import com.google.common.base.Strings;
import com.mpush.tools.Jsons;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.zk.cache.ZKRedisNodeCache;
import com.mpush.zk.node.ZKRedisNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * redis 监控
 */
public class ZKRedisNodeWatcher extends ZKNodeCacheWatcher {

    private final Logger logger = LoggerFactory.getLogger(ZKRedisNodeWatcher.class);

    private final ZKRedisNodeCache cache = new ZKRedisNodeCache();

    private void refresh() {
        String rawGroup = ZKClient.I.get(ZKPath.REDIS_SERVER.getRootPath());
        logger.warn("refresh zk redis node cache, data=" + rawGroup);
        if (Strings.isNullOrEmpty(rawGroup)) return;
        ZKRedisNode[] group = Jsons.fromJson(rawGroup, ZKRedisNode[].class);
        if (group == null) return;
        cache.addAll(Arrays.asList(group));
    }

    @Override
    protected void beforeWatch() {
        refresh();
    }

    @Override
    protected void onNodeAdded(String path, byte[] data) {
        refresh();
    }

    @Override
    protected void onNodeRemoved(String path, byte[] data) {
        refresh();
    }

    @Override
    protected void onNodeUpdated(String path, byte[] data) {
        refresh();
    }

    @Override
    public String watchPath() {
        return ZKPath.REDIS_SERVER.getRootPath();
    }

    public ZKRedisNodeCache getCache() {
        return cache;
    }
}
