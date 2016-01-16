package com.shinemo.mpush.cs.manage.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shinemo.mpush.cs.manage.ServerManage;
import com.shinemo.mpush.tools.zk.ServerApp;

public class ConnectionServerManage implements ServerManage{

	private static final Logger log = LoggerFactory.getLogger(ConnectionServerManage.class);

	private static Map<String,ServerApp> holder = Maps.newConcurrentMap();
	
	public void addOrUpdate(String fullPath,ServerApp app){
		holder.put(fullPath, app);
		printAppList();
	}
	
	public void remove(String fullPath){
		holder.remove(fullPath);
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
