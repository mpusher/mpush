package com.shinemo.mpush.tools.zk.listener.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.zk.listener.CallBack;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

/**
 * 当前应用下踢人的目录发生变化
 *
 */
public class KickPathListener implements CallBack{
	
	private static final Logger log = LoggerFactory.getLogger(KickPathListener.class);
	
	@Override
	public void handler(CuratorFramework client, TreeCacheEvent event, String path) {
		String data = "";
		if(event.getData()!=null){
			data = ToStringBuilder.reflectionToString(event.getData(), ToStringStyle.MULTI_LINE_STYLE);
		}
		if (Type.NODE_ADDED == event.getType()) {
			log.warn("path:" + path + ", node Add"+","+data);
		} else if (Type.NODE_REMOVED == event.getType()) {
			log.warn("path:" + path + ", node Remove"+","+data);
		} else if (Type.NODE_UPDATED == event.getType()) {
			log.warn("path:" + path + "," + "node update"+","+data);
		} else {
			log.warn("other path:" + path + "," + event.getType().name()+","+data);
		}
	}

	@Override
	public void initData(ServerManage manage) {
		
	}

}
