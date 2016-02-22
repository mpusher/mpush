package com.shinemo.mpush.common.manage.user;

import java.util.List;

import com.shinemo.mpush.api.RedisKey;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

//查询使用
public class UserManager {
	
	public void userOnline(String userId) {
    	String onlineKey = RedisKey.getUserOnlineKey();
    	RedisManage.sAdd(onlineKey, userId);
    	String offlineKey = RedisKey.getUserOfflineKey();
    	RedisManage.sRem(offlineKey, userId);
    }

    public void userOffline(String userId) {
        String onlineKey = RedisKey.getUserOnlineKey();
    	RedisManage.sRem(onlineKey, userId);
    	String offlineKey = RedisKey.getUserOfflineKey();
    	RedisManage.sAdd(offlineKey, userId);
    }
    
    //在线用户
    public long onlineUserNum(){
    	String onlineKey = RedisKey.getUserOnlineKey();
    	return RedisManage.sCard(onlineKey);
    }
    //离线用户
    public long offlineUserNum(){
    	String offlineKey = RedisKey.getUserOfflineKey();
    	return RedisManage.sCard(offlineKey);
    }
    
    //在线用户列表
    public List<String> onlineUserList(int start){
    	String onlineKey = RedisKey.getUserOnlineKey();
    	return RedisManage.sScan(onlineKey, start, String.class);
    }
    
    //离线用户
    public List<String> offlineUserList(int start){
    	String offlineKey = RedisKey.getUserOfflineKey();
    	return RedisManage.sScan(offlineKey, start, String.class);
    }
	
}
