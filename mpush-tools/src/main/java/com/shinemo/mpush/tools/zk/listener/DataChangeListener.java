package com.shinemo.mpush.tools.zk.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import com.shinemo.mpush.log.LogType;
import com.shinemo.mpush.log.LoggerManage;

public abstract class DataChangeListener implements TreeCacheListener{

	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
		String path = null == event.getData() ? "" : event.getData().getPath();
        if (path.isEmpty()) {
            return;
        }
        LoggerManage.log(LogType.ZK, "DataChangeListener:%s,%s,namespace:%s", path,listenerPath(),client.getNamespace());
        if(path.startsWith(listenerPath())){
            dataChanged(client, event, path);
        }
	}
	
	public abstract void initData();
	
	public abstract void dataChanged(CuratorFramework client, TreeCacheEvent event,String path) throws Exception; 
	
	public abstract String listenerPath();
}
