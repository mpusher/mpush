package com.shinemo.mpush.tools.redis;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.google.common.collect.Lists;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.zk.manage.ServerAppManage;

public class RedisGroupManage {
	
	
	private static final Logger log = LoggerFactory.getLogger(ServerAppManage.class);

	private static List<RedisGroup> holder = Lists.newArrayList();
	
	public static final RedisGroupManage instance = new RedisGroupManage();
	
	
	private RedisGroupManage() {
	}
	
	public void init(){
		
	}
	
	
	public List<RedisGroup> getAppList() {
		return Collections.unmodifiableList(holder);
	}
	
	private void printAppList(){
		for(RedisGroup app:holder){
			log.warn(ToStringBuilder.reflectionToString(app, ToStringStyle.DEFAULT_STYLE));
		}
	}
	
	

}
