package com.shinemo.mpush.common;

import java.util.List;


import com.google.common.collect.Lists;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.spi.ServiceContainer;
import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.ZkRegister;
import com.shinemo.mpush.tools.zk.listener.DataChangeListener;
import com.shinemo.mpush.tools.zk.listener.impl.RedisPathListener;

public abstract class AbstractClient {
	
    protected List<DataChangeListener> dataChangeListeners = Lists.newArrayList();
    
    protected ZkRegister zkRegister = ServiceContainer.getInstance(ZkRegister.class);
    
	public AbstractClient() {
		registerListener(new RedisPathListener());
	}
	
	
	public void registerListener(DataChangeListener listener){
		dataChangeListeners.add(listener);
	}

	//step1 启动 zk
	private void initZK(){
    	zkRegister.init();
	}
	
	//step2 获取redis
	private void initRedis(){
		boolean exist = zkRegister.isExisted(ZKPath.REDIS_SERVER.getPath());
        if (!exist) {
            List<RedisGroup> groupList = ConfigCenter.holder.redisGroups();
            zkRegister.registerPersist(ZKPath.REDIS_SERVER.getPath(), Jsons.toJson(groupList));
        }
	}
	
	//step3 注册listener
	private void registerListeners(){
		for(DataChangeListener listener:dataChangeListeners){
			zkRegister.registerListener(listener);
		}
	}
	
	//step4 初始化 listener data
	private void initListenerData(){
		for(DataChangeListener listener:dataChangeListeners){
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
