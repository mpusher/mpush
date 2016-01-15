package com.shinemo.mpush.cs.zk.listener.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.zk.listener.DataChangeListener;

/**
 *  push server 路径监控
 *
 */
public class PushServerPathListener extends DataChangeListener{
	
	private static final Logger log = LoggerFactory.getLogger(PushServerPathListener.class);

	@Override
	public void initData() {
		
	}

	@Override
	public void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) throws Exception {
		
	}

	@Override
	public String listenerPath() {
		return null;
	}
	

}
