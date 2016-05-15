package com.mpush.zk.listener;

import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKNodeManager;
import com.mpush.zk.ZKServerNode;
import com.mpush.tools.Jsons;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class ZKServerNodeListener<T extends ZKNodeManager<ZKServerNode>> extends ZKDataChangeListener {

    private final Logger log = LoggerFactory.getLogger(ZKServerNodeListener.class);

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
            log.warn(this.getClass().getSimpleName() + "other path:" + path + "," + event.getType().name() + "," + data);
        }
    }

    public void initData() {
        log.warn(" start init " + this.getClass().getSimpleName() + " server data");
        initData0();
        log.warn(" end init " + this.getClass().getSimpleName() + " server data");
    }

    public abstract String getRegisterPath();

    public abstract String getFullPath(String raw);

    public abstract T getManager();

    private void initData0() {
        // 获取机器列表
        List<String> rawData = ZKClient.I.getChildrenKeys(getRegisterPath());
        for (String raw : rawData) {
            String fullPath = getFullPath(raw);
            ZKServerNode app = getServerNode(fullPath);
            getManager().addOrUpdate(fullPath, app);
        }
    }

    private void dataRemove(ChildData data) {
        String path = data.getPath();
        getManager().remove(path);
    }

    private void dataAddOrUpdate(ChildData data) {
        String path = data.getPath();
        byte[] rawData = data.getData();
        ZKServerNode serverApp = Jsons.fromJson(rawData, ZKServerNode.class);
        getManager().addOrUpdate(path, serverApp);
    }

    private ZKServerNode getServerNode(String fullPath) {
        String rawApp = ZKClient.I.get(fullPath);
        ZKServerNode app = Jsons.fromJson(rawApp, ZKServerNode.class);
        return app;
    }


}
