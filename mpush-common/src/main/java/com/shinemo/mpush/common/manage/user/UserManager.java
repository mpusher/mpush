package com.shinemo.mpush.common.manage.user;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.RedisKey;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

//查询使用
public class UserManager {
	
	private static final Logger log = LoggerFactory.getLogger(UserManager.class);
	
	public void userOnline(String userId) {
    	String onlineKey = RedisKey.getUserOnlineKey();
    	RedisManage.zAdd(onlineKey, userId);
    	String offlineKey = RedisKey.getUserOfflineKey();
    	RedisManage.zRem(offlineKey, userId);
    	log.info("user online {}",userId);
    }

    public void userOffline(String userId) {
        String onlineKey = RedisKey.getUserOnlineKey();
    	RedisManage.zRem(onlineKey, userId);
    	String offlineKey = RedisKey.getUserOfflineKey();
    	RedisManage.zAdd(offlineKey, userId);
    	log.info("user offline {}",userId);
    }
    
    //在线用户
    public long onlineUserNum(){
    	String onlineKey = RedisKey.getUserOnlineKey();
    	return RedisManage.zCard(onlineKey);
    }
    
    //离线用户
    public long offlineUserNum(){
    	String offlineKey = RedisKey.getUserOfflineKey();
    	return RedisManage.zCard(offlineKey);
    }
    
    //在线用户列表
    public List<String> onlineUserList(int start,int size){
    	if(size<10){
    		size = 10;
    	}
    	String onlineKey = RedisKey.getUserOnlineKey();
    	return RedisManage.zrange(onlineKey, start, size-1, String.class);
    }
    
    //离线用户
    public List<String> offlineUserList(int start,int size){
    	if(size<10){
    		size = 10;
    	}
    	String offlineKey = RedisKey.getUserOfflineKey();
    	return RedisManage.zrange(offlineKey, start, size-1, String.class);
    }
	
}
