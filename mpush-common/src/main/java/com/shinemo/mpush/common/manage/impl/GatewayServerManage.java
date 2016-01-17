package com.shinemo.mpush.common.manage.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shinemo.mpush.common.app.impl.GatewayServerApplication;
import com.shinemo.mpush.common.manage.ServerManage;

public class GatewayServerManage implements ServerManage<GatewayServerApplication>{

	private static final Logger log = LoggerFactory.getLogger(GatewayServerManage.class);

	private static Map<String,GatewayServerApplication> holder = Maps.newConcurrentMap();
	
	@Override
	public void addOrUpdate(String fullPath,GatewayServerApplication application){
		holder.put(fullPath, application);
		printList();
	}
	
	@Override
	public void remove(String fullPath){
		holder.remove(fullPath);
		printList();
	}
	
	@Override
	public Collection<GatewayServerApplication> getList() {
		return Collections.unmodifiableCollection(holder.values());
	}
	
	private void printList(){
		for(GatewayServerApplication app:holder.values()){
			log.warn(ToStringBuilder.reflectionToString(app, ToStringStyle.DEFAULT_STYLE));
		}
	}
	
	public GatewayServerApplication get(String fullpath){
		return holder.get(fullpath);
	}
	
}
