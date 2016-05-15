package com.mpush.zk.listener;

import com.google.common.base.Strings;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.tools.Jsons;
import com.mpush.tools.redis.RedisGroup;
import com.mpush.tools.redis.RedisRegister;
import com.mpush.tools.spi.ServiceContainer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * redis 监控
 */
public class ZKRedisNodeListener extends ZKDataChangeListener {
    private final Logger logger = LoggerFactory.getLogger(ZKRedisNodeListener.class);

    private final RedisRegister redisRegister = ServiceContainer.load(RedisRegister.class);

    // 获取redis列表
    private void _initData() {
        logger.warn("start init redis data");
        List<RedisGroup> group = getRedisGroup(ZKPath.REDIS_SERVER.getPath());
        redisRegister.init(group);
        logger.warn("end init redis data");
    }

    private void dataRemove(ChildData data) {
        _initData();
    }

    private void dataAddOrUpdate(ChildData data) {
        _initData();
    }

    @SuppressWarnings("unchecked")
    private List<RedisGroup> getRedisGroup(String fullPath) {
        String rawGroup = ZKClient.I.get(fullPath);
        if (Strings.isNullOrEmpty(rawGroup))
            return Collections.EMPTY_LIST;
        List<RedisGroup> group = Jsons.fromJsonToList(rawGroup, RedisGroup[].class);
        if (group == null)
            return Collections.EMPTY_LIST;
        return group;
    }

    @Override
    public void initData() {
        _initData();
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
            logger.warn("RedisPathListener other path:" + data + "," + event.getType().name() + "," + data);
        }

    }

    @Override
    public String listenerPath() {
        return ZKPath.REDIS_SERVER.getPath();
    }
}
