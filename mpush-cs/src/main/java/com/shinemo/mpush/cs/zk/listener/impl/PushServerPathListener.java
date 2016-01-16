package com.shinemo.mpush.cs.zk.listener.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.listener.DataChangeListener;

/**
 *  push server 路径监控
 *
 */
public class PushServerPathListener extends DataChangeListener{
	
	private static final Logger log = LoggerFactory.getLogger(PushServerPathListener.class);

	@Override
	public void initData() {
		log.warn("start init push server data");
		log.warn("end init push server data");
	}

	@Override
	public void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) throws Exception {
		String data = "";
		if (event.getData() != null) {
			data = ToStringBuilder.reflectionToString(event.getData(), ToStringStyle.MULTI_LINE_STYLE);
		}
		log.warn("ConnPathListener other path:" + path + "," + event.getType().name() + "," + data);
	}

	@Override
	public String listenerPath() {
		return ZKPath.PUSH_SERVER.getWatchPath();
	}
	

}
