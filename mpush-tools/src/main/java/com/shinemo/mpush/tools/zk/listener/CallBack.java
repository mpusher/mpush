package com.shinemo.mpush.tools.zk.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

public interface CallBack {
	
	public void handler(CuratorFramework client, TreeCacheEvent event,String path);
	
}
