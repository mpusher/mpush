package com.shinemo.mpush.tools.zk.manage;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shinemo.mpush.tools.zk.ServerApp;

/**
 * 系统中当前可用的app列表
 *
 */
public class ServerAppManage {
	
	private static final Logger log = LoggerFactory.getLogger(ServerAppManage.class);

	private static Map<String,ServerApp> holder = Maps.newConcurrentMap();
	
	public static final ServerAppManage instance = new ServerAppManage();
	
	private ServerAppManage() {
	}

	public void addOrUpdate(String fullPath,ServerApp app){
		printAppList();
	}
	
	public void remove(String fullPath){
		printAppList();
	}
	
	public void init(){
		printAppList();
	}
	
	public Collection<ServerApp> getAppList() {
		return Collections.unmodifiableCollection(holder.values());
	}
	
	private void printAppList(){
		for(ServerApp app:holder.values()){
			log.warn(ToStringBuilder.reflectionToString(app, ToStringStyle.DEFAULT_STYLE));
		}
	}
}
