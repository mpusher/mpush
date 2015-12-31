package com.shinemo.mpush.tools.zk.listener.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.listener.CallBack;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

/**
 *注册的应用的发生变化 
 *
 */
public class ConnectionPathListener implements CallBack{
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionPathListener.class);
	
	private List<ServerApp> appList = new ArrayList<ServerApp>();
	
	@Override
	public void handler(CuratorFramework client, TreeCacheEvent event, String path) {
		if (Type.NODE_ADDED == event.getType()) {
			log.warn("path:" + path + ", node Add");
		} else if (Type.NODE_REMOVED == event.getType()) {
			log.warn("path:" + path + ", node Remove");
		} else if (Type.NODE_UPDATED == event.getType()) {
			log.warn("path:" + path + "," + "node update");
		} else {
			log.warn("path:" + path + "," + event.getType().name());
		}
	}

	@Override
	public void initData(ServerManage manage) {
		log.warn("start init data");
		List<String> rawData = manage.getZkUtil().getChildrenKeys(PathEnum.CONNECTION_SERVER_ALL_HOST.getPath());
		List<ServerApp> newAppList = new ArrayList<ServerApp>();
		for(String raw:rawData){
			ServerApp app = Jsons.fromJson(raw, ServerApp.class);
			newAppList.add(app);
		}
		appList = newAppList;
		log.warn("end init data");
	}
	
	public List<ServerApp> getAppList(){
		return appList;
	}

}
