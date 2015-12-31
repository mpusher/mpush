package com.shinemo.mpush.tools.zk.listener.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZkUtil;
import com.shinemo.mpush.tools.zk.listener.CallBack;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

/**
 *注册的应用的发生变化 
 *
 */
public class ConnectionPathListener implements CallBack{
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionPathListener.class);
	
	private static Map<String,ServerApp> holder = Maps.newConcurrentMap();
	
	@Override
	public void handler(CuratorFramework client, TreeCacheEvent event, String path) {
		String data = "";
		if(event.getData()!=null){
			data = ToStringBuilder.reflectionToString(event.getData(), ToStringStyle.MULTI_LINE_STYLE);
		}
		if (Type.NODE_ADDED == event.getType()) {
			dataAddOrUpdate(event.getData());
			log.warn("ConnectionPathListener path:" + path + ", node Add"+","+data);
		} else if (Type.NODE_REMOVED == event.getType()) {
			dataRemove(event.getData());
			log.warn("ConnectionPathListener path:" + path + ", node Remove"+","+data);
		} else if (Type.NODE_UPDATED == event.getType()) {
			dataAddOrUpdate(event.getData());
			log.warn("ConnectionPathListener path:" + path + "," + "node update"+","+data);
		} else {
			log.warn("ConnectionPathListener other path:" + path + "," + event.getType().name()+","+data);
		}
	}

	@Override
	public void initData(ServerManage manage) {
		log.warn("start init app data");
		getData();
		printAppList();
		log.warn("end init app data");
	}
	
	private void getData(){
		//获取机器列表
		List<String> rawData = ZkUtil.instance.getChildrenKeys(PathEnum.CONNECTION_SERVER_ALL_HOST.getPath());
		for(String raw:rawData){
			String fullPath = PathEnum.CONNECTION_SERVER_ALL_HOST.getPathByName(raw);
			ServerApp app = getServerApp(raw);
			holder.put(fullPath, app);
		}
	}
	
	private void dataRemove(ChildData data){
		String path = data.getPath();
		holder.remove(path);
		printAppList();
	}
	
	private void dataAddOrUpdate(ChildData data){
		String path = data.getPath();
		byte[] rawData = data.getData();
		ServerApp serverApp = Jsons.fromJson(rawData, ServerApp.class);
		holder.put(path, serverApp);
		printAppList();
	}
	
	private ServerApp getServerApp(String fullPath){
		String rawApp = ZkUtil.instance.get(fullPath);
		ServerApp app = Jsons.fromJson(rawApp, ServerApp.class);
		return app;
	}
	
	public Collection<ServerApp> getAppList(){
		return Collections.unmodifiableCollection(holder.values());
	}

	private void printAppList(){
		for(ServerApp app:holder.values()){
			log.warn(ToStringBuilder.reflectionToString(app, ToStringStyle.DEFAULT_STYLE));
		}
	}
}
