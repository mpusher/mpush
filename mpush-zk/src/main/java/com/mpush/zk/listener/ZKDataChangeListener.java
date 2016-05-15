package com.mpush.zk.listener;

import com.mpush.log.LogType;
import com.mpush.log.LoggerManage;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

public abstract class ZKDataChangeListener implements TreeCacheListener {

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        String path = null == event.getData() ? "" : event.getData().getPath();
        if (path.isEmpty()) {
            return;
        }
        LoggerManage.log(LogType.ZK, "DataChangeListener:{},{},namespace:{}", path, listenerPath(), client.getNamespace());
        if (path.startsWith(listenerPath())) {
            dataChanged(client, event, path);
        }
    }

    public abstract void initData();

    public abstract void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) throws Exception;

    public abstract String listenerPath();
}
