package com.shinemo.mpush.tools.zk.manage;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.zk.PathEnum;
import com.shinemo.mpush.tools.zk.ZkConfig;
import com.shinemo.mpush.tools.zk.ZkUtil;
import com.shinemo.mpush.tools.zk.listener.CallBack;
import com.shinemo.mpush.tools.zk.listener.ListenerDispatcher;

public class ServerManage {

	private static final Logger log = LoggerFactory.getLogger(ServerManage.class);

	private static final ZkConfig zkConfig = new ZkConfig(Constants.ZK_IPS, Constants.ZK_NAME_SPACE);

	private static final ZkUtil zkUtil = new ZkUtil(zkConfig);

	static {
		zkUtil.init();
	}


	public void start(String ip) {
		//注册机器到zk中
		zkUtil.registerEphemeralSequential(PathEnum.CONNECTION_SERVER_ALL_HOST.getPath());
		// 注册连接状态监听器
		registerConnectionLostListener();
		// 注册节点数据变化
		registerDataChange(ListenerDispatcher.instance);
	}

	// 注册连接状态监听器
	private void registerConnectionLostListener() {
		zkUtil.getClient().getConnectionStateListenable().addListener(new ConnectionStateListener() {

			@Override
			public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
				if (ConnectionState.LOST == newState) {
					log.warn(InetAddressUtil.getInetAddress() + ", lost connection");
				} else if (ConnectionState.RECONNECTED == newState) {
					log.warn(InetAddressUtil.getInetAddress() + ", reconnected");
				}
			}
		});
	}

	// 注册节点数据变化
	private void registerDataChange(final CallBack callBack) {
		zkUtil.getCache().getListenable().addListener(new TreeCacheListener() {

			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				String path = null == event.getData() ? "" : event.getData().getPath();
				if (path.isEmpty()) {
					return;
				}
				callBack.handler(client, event, path);
			}
		});
	}
	
	public CuratorFramework getClient() {
		return zkUtil.getClient();
	}
	
    public TreeCache getCache() {
        return zkUtil.getCache();
    }
    
    public void close(){
    	zkUtil.close();
    }

}
