package com.shinemo.mpush.tools.zk.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

import com.shinemo.mpush.tools.zk.manage.ServerManage;

public interface CallBack {
	
	/**
	 * 处理目录发生变化的事件
	 * @param client
	 * @param event
	 * @param path
	 */
	public void handler(CuratorFramework client, TreeCacheEvent event,String path);
	
	/**
	 * 应用起来的时候初始化数据
	 * @param manage
	 */
	public void initData(ServerManage manage);
	
}
