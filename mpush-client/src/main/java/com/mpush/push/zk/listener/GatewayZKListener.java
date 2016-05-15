package com.mpush.push.zk.listener;

import com.mpush.push.zk.manager.GatewayZKNodeManager;
import com.mpush.zk.ZKPath;
import com.mpush.zk.listener.ZKServerNodeListener;

/**
 * gateway server 应用  监控
 */
public class GatewayZKListener extends ZKServerNodeListener<GatewayZKNodeManager> {

    private final GatewayZKNodeManager manager = new GatewayZKNodeManager();

    @Override
    public String listenerPath() {
        return ZKPath.GATEWAY_SERVER.getWatchPath();
    }

    @Override
    public GatewayZKNodeManager getManager() {
        return manager;
    }

    @Override
    public String getRegisterPath() {
        return ZKPath.GATEWAY_SERVER.getPath();
    }

    @Override
    public String getFullPath(String raw) {
        return ZKPath.GATEWAY_SERVER.getFullPath(raw);
    }


}
