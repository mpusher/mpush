package com.shinemo.mpush.tools.zk.listener;

import java.util.Iterator;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.listener.impl.ConnectionPathListener;
import com.shinemo.mpush.tools.zk.listener.impl.KickPathListener;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

public class ListenerDispatcher implements CallBack {

	private static final Logger log = LoggerFactory.getLogger(ListenerDispatcher.class);

	private Map<String, CallBack> holder = Maps.newTreeMap();

	public ListenerDispatcher(ServerApp app) {
		holder.put(PathEnum.CONNECTION_SERVER_ALL_HOST.getPathByIp(app.getIp()), new ConnectionPathListener());
		holder.put(PathEnum.CONNECTION_SERVER_KICK.getPathByIp(app.getIp()), new KickPathListener());
	}

	@Override
	public void handler(CuratorFramework client, TreeCacheEvent event, String path) {

		Iterator<Map.Entry<String, CallBack>> it = holder.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, CallBack> entry = it.next();
			if (path.startsWith(entry.getKey())) {
				entry.getValue().handler(client, event, path);
			} else { // 其他路径的事件，暂时不关心
				log.warn("path:" + path + "," + event.getType().name());
			}
		}

	}

	@Override
	public void initData(ServerManage manage) {

		Iterator<Map.Entry<String, CallBack>> it = holder.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, CallBack> entry = it.next();
			entry.getValue().initData(manage);
		}
		
	}
	
	public CallBack getListener(PathEnum pathEnum,String ip){
		return holder.get(pathEnum.getPathByIp(ip));
	}

}
