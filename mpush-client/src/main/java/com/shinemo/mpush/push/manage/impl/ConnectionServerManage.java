package com.shinemo.mpush.push.manage.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shinemo.mpush.common.manage.ServerManage;
import com.shinemo.mpush.conn.client.ConnectionServerApplication;

public class ConnectionServerManage implements ServerManage<ConnectionServerApplication>{

	private static final Logger log = LoggerFactory.getLogger(ConnectionServerManage.class);

	private static Map<String,ConnectionServerApplication> holder = Maps.newConcurrentMap();
	
	@Override
	public void addOrUpdate(String fullPath,ConnectionServerApplication application){
		if(StringUtils.isNotBlank(fullPath)&&application!=null){
			holder.put(fullPath, application);
		}else{
			log.error("fullPath is null or application is null"+fullPath==null?"fullpath is null":fullPath+","+application==null?"application is null":"application is not null");
		}

		printList();
	}
	
	@Override
	public void remove(String fullPath){
		holder.remove(fullPath);
		printList();
	}
	
	@Override
	public Collection<ConnectionServerApplication> getList() {
		return Collections.unmodifiableCollection(holder.values());
	}
	
	private void printList(){
		for(ConnectionServerApplication app:holder.values()){
			log.warn(ToStringBuilder.reflectionToString(app, ToStringStyle.DEFAULT_STYLE));
		}
	}
	
}
