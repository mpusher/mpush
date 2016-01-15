package com.shinemo.mpush.tools.zk.listener.impl;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Strings;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.redis.RedisRegister;
import com.shinemo.mpush.tools.spi.ServiceContainer;
import com.shinemo.mpush.tools.zk.ZKPath;
import com.shinemo.mpush.tools.zk.ZkRegister;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

/**
 * 注册的应用的发生变化
 */
public class RedisPathListener implements TreeCacheListener {
    private static final Logger log = LoggerFactory.getLogger(RedisPathListener.class);

    private static final ZkRegister zkRegister = ServiceContainer.getInstance(ZkRegister.class);
    
    private static final RedisRegister redisRegister = ServiceContainer.getInstance(RedisRegister.class);
    
    @Override
    public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
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
            log.warn("ConnPathListener other path:" + data + "," + event.getType().name() + "," + data);
        }
    }

    public void initData(ServerManage manage) {
        log.warn("start init redis data");
        _initData();
        log.warn("end init redis data");
    }

    private void _initData() {
        //获取redis列表
        List<RedisGroup> group = getRedisGroup(ZKPath.REDIS_SERVER.getPath());
        redisRegister.init(group);
    }

    private void dataRemove(ChildData data) {
        _initData();
    }

    private void dataAddOrUpdate(ChildData data) {
        _initData();
    }

    private List<RedisGroup> getRedisGroup(String fullPath) {
        String rawGroup = zkRegister.get(fullPath);
        if (Strings.isNullOrEmpty(rawGroup)) return Collections.EMPTY_LIST;
        List<RedisGroup> group = Jsons.fromJsonToList(rawGroup, RedisGroup[].class);
        if (group == null) return Collections.EMPTY_LIST;
        return group;
    }
}
