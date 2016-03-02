package com.shinemo.mpush.common.manage.user;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.RedisKey;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

//查询使用
public class UserManager {
	
    private static final String EXTRANET_ADDRESS = MPushUtil.getExtranetAddress();
    
    private static final String ONLINE_KEY = RedisKey.getUserOnlineKey(EXTRANET_ADDRESS); 
	
	private static final Logger log = LoggerFactory.getLogger(UserManager.class);
	
	public void init(){
		RedisManage.del(ONLINE_KEY);
		log.info("init redis key:{}"+ONLINE_KEY);
	}
	
	public void userOnline(String userId) {
    	RedisManage.zAdd(ONLINE_KEY, userId);
    	log.info("user online {}",userId);
    }

    public void userOffline(String userId) {
    	RedisManage.zRem(ONLINE_KEY, userId);
    	log.info("user offline {}",userId);
    }
    
    //在线用户
    public long onlineUserNum(){
    	return RedisManage.zCard(ONLINE_KEY);
    }
    
    //在线用户列表
    public List<String> onlineUserList(int start,int size){
    	if(size<10){
    		size = 10;
    	}
    	return RedisManage.zrange(ONLINE_KEY, start, size-1, String.class);
    }
    
}
