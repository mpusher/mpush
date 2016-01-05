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

import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.redis.RedisGroupManage;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ZkUtil;
import com.shinemo.mpush.tools.zk.listener.CallBack;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

/**
 *注册的应用的发生变化 
 *
 */
public class RedisPathListener implements CallBack{
	
	private static final Logger log = LoggerFactory.getLogger(RedisPathListener.class);
	
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
		log.warn("start init redis data");
		_initData();
		log.warn("end init redis data");
	}
	
	private void _initData(){
		//获取redis列表
		List<RedisGroup> group = getRedisGroup(PathEnum.CONNECTION_SERVER_REDIS.getPathByIp(InetAddressUtil.getInetAddress()));
		RedisGroupManage.instance.init(group);
	}
	
	private void dataRemove(ChildData data){
		_initData();
	}
	
	private void dataAddOrUpdate(ChildData data){
		_initData();
	}
	
	private List<RedisGroup> getRedisGroup(String fullPath){
		String rawGroup = ZkUtil.instance.get(fullPath);
		List<RedisGroup> group = Jsons.fromJsonToList(rawGroup, RedisGroup[].class);
		return group;
	}


}
