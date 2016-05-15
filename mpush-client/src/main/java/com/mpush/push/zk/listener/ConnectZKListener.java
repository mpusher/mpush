package com.mpush.push.zk.listener;


import com.mpush.zk.ZKPath;
import com.mpush.zk.listener.ZKServerNodeListener;
import com.mpush.push.zk.manager.ConnectZKNodeManager;

/**
 * connection server 应用  监控
 */
public class ConnectZKListener extends ZKServerNodeListener<ConnectZKNodeManager> {

    private final ConnectZKNodeManager manager = new ConnectZKNodeManager();

    @Override
    public String listenerPath() {
        return ZKPath.CONNECTION_SERVER.getWatchPath();
    }

    @Override
    public String getRegisterPath() {
        return ZKPath.CONNECTION_SERVER.getPath();
    }

    @Override
    public ConnectZKNodeManager getManager() {
        return manager;
    }

    @Override
    public String getFullPath(String raw) {
        return ZKPath.CONNECTION_SERVER.getFullPath(raw);
    }

}
