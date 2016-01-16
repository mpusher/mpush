package com.shinemo.mpush.cs.zk.listener.impl;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.cs.manage.ConnectionServerManage;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.spi.ServiceContainer;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.ZkRegister;
import com.shinemo.mpush.tools.zk.listener.DataChangeListener;

/**
 * connection server 应用  监控
 * 
 */
public class ConnectionServerPathListener extends DataChangeListener{
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionServerPathListener.class);

	private static ZkRegister zkRegister = ServiceContainer.getInstance(ZkRegister.class);

	@Override
	public void initData() {
		log.warn("start init connection server data");
		_initData();
		log.warn("end init connection server data");
	}

	private void _initData() {
		// 获取机器列表
		List<String> rawData = zkRegister.getChildrenKeys(ZKPath.CONNECTION_SERVER.getPath());
		for (String raw : rawData) {
			String fullPath = ZKPath.CONNECTION_SERVER.getFullPath(raw);
			ServerApp app = getServerApp(fullPath);
			ConnectionServerManage.instance.addOrUpdate(fullPath, app);
		}
	}

	private void dataRemove(ChildData data) {
		String path = data.getPath();
		ConnectionServerManage.instance.remove(path);
	}

	private void dataAddOrUpdate(ChildData data) {
		String path = data.getPath();
		byte[] rawData = data.getData();
		ServerApp serverApp = Jsons.fromJson(rawData, ServerApp.class);
		ConnectionServerManage.instance.addOrUpdate(path, serverApp);
	}

	private ServerApp getServerApp(String fullPath) {
		String rawApp = zkRegister.get(fullPath);
		ServerApp app = Jsons.fromJson(rawApp, ServerApp.class);
		return app;
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
			log.warn("ConnPathListener other path:" + path + "," + event.getType().name() + "," + data);
		}

	}

	@Override
	public String listenerPath() {
		return ZKPath.CONNECTION_SERVER.getWatchPath();
	}

}
