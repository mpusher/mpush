package com.mpush.common;

import java.util.List;


import com.google.common.collect.Lists;
import com.mpush.tools.redis.RedisGroup;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.tools.Jsons;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.zk.listener.ZKDataChangeListener;
import com.mpush.zk.listener.ZKRedisNodeListener;

public abstract class AbstractClient {
	
    protected List<ZKDataChangeListener> dataChangeListeners = Lists.newArrayList();
    
    protected ZKClient zkClient = ZKClient.I;
    
	public AbstractClient() {
		registerListener(new ZKRedisNodeListener());
	}
	
	
	public void registerListener(ZKDataChangeListener listener){
		dataChangeListeners.add(listener);
	}

	//step1 启动 zk
	private void initZK(){
    	zkClient.init();
	}
	
	//step2 获取redis
	private void initRedis(){
		boolean exist = zkClient.isExisted(ZKPath.REDIS_SERVER.getPath());
        if (!exist) {
            List<RedisGroup> groupList = ConfigCenter.holder.redisGroups();
            zkClient.registerPersist(ZKPath.REDIS_SERVER.getPath(), Jsons.toJson(groupList));
        }
	}
	
	//step3 注册listener
	private void registerListeners(){
		for(ZKDataChangeListener listener:dataChangeListeners){
			zkClient.registerListener(listener);
		}
	}
	
	//step4 初始化 listener data
	private void initListenerData(){
		for(ZKDataChangeListener listener:dataChangeListeners){
			listener.initData();
		}
	}
	
	public void start(){
		initZK();
		initRedis();
		registerListeners();
		initListenerData();
	}
}
