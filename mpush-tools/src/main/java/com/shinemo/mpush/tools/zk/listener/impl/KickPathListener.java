package com.shinemo.mpush.tools.zk.listener.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.zk.listener.CallBack;

/**
 * 当前应用下踢人的目录发生变化
 *
 */
public class KickPathListener implements CallBack{
	
	private static final Logger log = LoggerFactory.getLogger(KickPathListener.class);
	
	@Override
	public void handler(CuratorFramework client, TreeCacheEvent event, String path) {
		if (Type.NODE_ADDED == event.getType()) {
			log.warn("path:" + path + ", node Add" +","+event.getData().getData());
		} else if (Type.NODE_REMOVED == event.getType()) {
			log.warn("path:" + path + ", node Remove"+","+event.getData().getData());
		} else if (Type.NODE_UPDATED == event.getType()) {
			log.warn("path:" + path + "," + "node update"+","+event.getData().getData());
		} else {
			log.warn("path:" + path + "," + event.getType().name()+","+event.getData().getData());
		}
	}

}
