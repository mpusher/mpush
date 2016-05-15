package com.mpush.common;

import com.google.common.collect.Lists;
import com.mpush.tools.Jsons;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.redis.RedisGroup;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.zk.listener.ZKDataChangeListener;
import com.mpush.zk.listener.ZKRedisNodeListener;

import java.util.List;

public abstract class AbstractClient {

    protected List<ZKDataChangeListener> dataChangeListeners = Lists.newArrayList();

    protected ZKClient zkClient = ZKClient.I;

    public AbstractClient() {
        registerListener(new ZKRedisNodeListener());
    }

    public void registerListener(ZKDataChangeListener listener) {
        dataChangeListeners.add(listener);
    }

    //step2 获取redis
    private void initRedis() {
        boolean exist = zkClient.isExisted(ZKPath.REDIS_SERVER.getPath());
        if (!exist) {
            List<RedisGroup> groupList = ConfigCenter.I.redisGroups();
            zkClient.registerPersist(ZKPath.REDIS_SERVER.getPath(), Jsons.toJson(groupList));
        }
    }

    //step3 注册listener
    private void registerListeners() {
        for (ZKDataChangeListener listener : dataChangeListeners) {
            zkClient.registerListener(listener);
        }
    }

    //step4 初始化 listener data
    private void initListenerData() {
        for (ZKDataChangeListener listener : dataChangeListeners) {
            listener.initData();
        }
    }

    public void start() {
        initRedis();
        registerListeners();
        initListenerData();
    }
}
