package com.mpush.zk.listener;

import com.google.common.base.Strings;
import com.mpush.tools.log.Logs;
import com.mpush.zk.ZKClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

public abstract class ZKNodeCacheWatcher implements TreeCacheListener {

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        ChildData data = event.getData();
        if (data == null) return;
        String path = data.getPath();
        if (Strings.isNullOrEmpty(path)) return;
        if (path.startsWith(watchPath())) {
            switch (event.getType()) {
                case NODE_ADDED:
                    onNodeAdded(path, data.getData());
                    break;
                case NODE_REMOVED:
                    onNodeRemoved(path, data.getData());
                    break;
                case NODE_UPDATED:
                    onNodeUpdated(path, data.getData());
                    break;
            }
        }
        Logs.ZK.info("ZK node data change, name={}, listener={}, ns={}", path, watchPath(), client.getNamespace());
    }

    public final void beginWatch() {
        beforeWatch();
        ZKClient.I.registerListener(this);
    }

    public abstract String watchPath();

    protected void beforeWatch() {

    }

    protected void onNodeAdded(String path, byte[] data) {

    }

    protected void onNodeRemoved(String path, byte[] data) {

    }

    protected void onNodeUpdated(String path, byte[] data) {

    }
}
