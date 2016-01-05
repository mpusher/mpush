package com.shinemo.mpush.tools.redis.manage;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.shinemo.mpush.tools.RandomUtil;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.redis.RedisNode;
import com.shinemo.mpush.tools.zk.manage.ServerAppManage;

public class RedisGroupManage {
	
	
	private static final Logger log = LoggerFactory.getLogger(ServerAppManage.class);

	private static List<RedisGroup> holder = Lists.newArrayList();
	
	public static final RedisGroupManage instance = new RedisGroupManage();
	
	
	private RedisGroupManage() {
	}
	
	public void init(List<RedisGroup> group){
		holder = group;
		printGroupList();
	}
	
	
	public List<RedisGroup> getGroupList() {
		return Collections.unmodifiableList(holder);
	}
	
	private void printGroupList(){
		for(RedisGroup app:holder){
			log.warn(ToStringBuilder.reflectionToString(app, ToStringStyle.DEFAULT_STYLE));
		}
	}
	
	public int groupSize(){
		return holder.size();
	}
	
	/**
	 * 随机获取一个redis 实例
	 * @param key
	 * @return
	 */
	public RedisNode randomGetRedisNode(String key){
		int i = RandomUtil.random(groupSize());
		RedisGroup group = holder.get(i);
		return group.get(key);
	}
	
	/**
	 * 写操作的时候，获取所有redis 实例
	 * @param key
	 * @return
	 */
	public List<RedisNode> hashSet(String key){
		List<RedisNode> nodeList = Lists.newArrayList();
		for(RedisGroup group:holder){
			RedisNode node = group.get(key);
			nodeList.add(node);
		}
		return nodeList;
	}
	

}
