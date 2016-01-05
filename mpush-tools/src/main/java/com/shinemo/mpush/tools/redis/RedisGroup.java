package com.shinemo.mpush.tools.redis;

import java.util.List;

import com.google.common.collect.Lists;


/**
 * redis 组 
 *
 */
public class RedisGroup {
	
	private List<RedisNode> redisNodeList;

	public List<RedisNode> getRedisNodeList() {
		return redisNodeList;
	}

	public void setRedisNodeList(List<RedisNode> redisNodeList) {
		this.redisNodeList = redisNodeList;
	}
	
	public void addRedisNode(RedisNode node){
		if(redisNodeList==null){
			redisNodeList = Lists.newArrayList();
		}
		redisNodeList.add(node);
	}
	
	public void remove(int i){
		if(redisNodeList!=null){
			redisNodeList.remove(i);
		}
	}
	
	public void clear(){
		if(redisNodeList!=null){
			redisNodeList.clear();
		}
	}
	
	
}
