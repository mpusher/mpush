package com.shinemo.mpush.tools.zk.listener.impl;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Strings;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.redis.RedisRegister;
import com.shinemo.mpush.tools.spi.ServiceContainer;
import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.ZkRegister;
import com.shinemo.mpush.tools.zk.listener.DataChangeListener;

/**
 * redis 监控
 */
public class RedisPathListener extends  DataChangeListener {
	private static final Logger log = LoggerFactory.getLogger(RedisPathListener.class);

	private final ZkRegister zkRegister = ServiceContainer.getInstance(ZkRegister.class);

	private final RedisRegister redisRegister = ServiceContainer.getInstance(RedisRegister.class);
	
	// 获取redis列表
	private void _initData() {
		log.warn("start init redis data");
		List<RedisGroup> group = getRedisGroup(ZKPath.REDIS_SERVER.getPath());
		redisRegister.init(group);
		log.warn("end init redis data");
	}

	private void dataRemove(ChildData data) {
		_initData();
	}

	private void dataAddOrUpdate(ChildData data) {
		_initData();
	}

	@SuppressWarnings("unchecked")
	private List<RedisGroup> getRedisGroup(String fullPath) {
		String rawGroup = zkRegister.get(fullPath);
		if (Strings.isNullOrEmpty(rawGroup))
			return Collections.EMPTY_LIST;
		List<RedisGroup> group = Jsons.fromJsonToList(rawGroup, RedisGroup[].class);
		if (group == null)
			return Collections.EMPTY_LIST;
		return group;
	}

	@Override
	public void initData() {
		_initData();
	}

	@Override
	public void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) throws Exception {
		
		String data = "";
		if (event.getData() != null) {
			data = ToStringBuilder.reflectionToString(event.getData(), ToStringStyle.MULTI_LINE_STYLE);
		}
		if (Type.NODE_ADDED == event.getType()) {
			dataAddOrUpdate(event.getData());
		} else if (Type.NODE_REMOVED == event.getType()) {
			dataRemove(event.getData());
		} else if (Type.NODE_UPDATED == event.getType()) {
			dataAddOrUpdate(event.getData());
		} else {
			log.warn("RedisPathListener other path:" + data + "," + event.getType().name() + "," + data);
		}
		
	}
	
	@Override
	public String listenerPath() {
		return ZKPath.REDIS_SERVER.getPath();
	}
}
