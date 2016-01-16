package com.shinemo.mpush.tools.zk.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataChangeListener implements TreeCacheListener{

	private static final Logger log = LoggerFactory.getLogger(DataChangeListener.class);
	
	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
		String path = null == event.getData() ? "" : event.getData().getPath();
        if (path.isEmpty()) {
            return;
        }
        
        log.warn("DataChangeListener:"+path+",listenerPath:"+listenerPath());
        
        if(path.startsWith(listenerPath())){
            dataChanged(client, event, path);
        }
	}
	
	public abstract void initData();
	
	public abstract void dataChanged(CuratorFramework client, TreeCacheEvent event,String path) throws Exception; 
	
	public abstract String listenerPath();
}
