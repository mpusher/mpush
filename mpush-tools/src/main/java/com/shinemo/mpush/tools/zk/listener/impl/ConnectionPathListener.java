package com.shinemo.mpush.tools.zk.listener.impl;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZkUtil;
import com.shinemo.mpush.tools.zk.listener.CallBack;
import com.shinemo.mpush.tools.zk.manage.ServerAppManage;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

/**
 *注册的应用的发生变化 
 *
 */
public class ConnectionPathListener implements CallBack{
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionPathListener.class);
	
	@Override
	public void handler(CuratorFramework client, TreeCacheEvent event, String path) {
		String data = "";
		if(event.getData()!=null){
			data = ToStringBuilder.reflectionToString(event.getData(), ToStringStyle.MULTI_LINE_STYLE);
		}
		if (Type.NODE_ADDED == event.getType()) {
			dataAddOrUpdate(event.getData());
		} else if (Type.NODE_REMOVED == event.getType()) {
			dataRemove(event.getData());
		} else if (Type.NODE_UPDATED == event.getType()) {
			dataAddOrUpdate(event.getData());
		} else {
			log.warn("ConnectionPathListener other path:" + path + "," + event.getType().name()+","+data);
		}
	}

	@Override
	public void initData(ServerManage manage) {
		log.warn("start init app data");
		_initData();
		log.warn("end init app data");
	}
	
	private void _initData(){
		//获取机器列表
		List<String> rawData = ZkUtil.instance.getChildrenKeys(PathEnum.CONNECTION_SERVER.getPath());
		for(String raw:rawData){
			String fullPath = PathEnum.CONNECTION_SERVER.getPathByName(raw);
			ServerApp app = getServerApp(fullPath);
			ServerAppManage.instance.addOrUpdate(fullPath, app);
		}
	}
	
	private void dataRemove(ChildData data){
		String path = data.getPath();
		ServerAppManage.instance.remove(path);
	}
	
	private void dataAddOrUpdate(ChildData data){
		String path = data.getPath();
		byte[] rawData = data.getData();
		ServerApp serverApp = Jsons.fromJson(rawData, ServerApp.class);
		ServerAppManage.instance.addOrUpdate(path, serverApp);
	}
	
	private ServerApp getServerApp(String fullPath){
		String rawApp = ZkUtil.instance.get(fullPath);
		ServerApp app = Jsons.fromJson(rawApp, ServerApp.class);
		return app;
	}


}
