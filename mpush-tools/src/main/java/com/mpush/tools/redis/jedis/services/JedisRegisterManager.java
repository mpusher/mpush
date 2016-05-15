package com.mpush.tools.redis.jedis.services;

import com.google.common.collect.Lists;
import com.mpush.log.LogType;
import com.mpush.log.LoggerManage;
import com.mpush.tools.redis.RedisGroup;
import com.mpush.tools.redis.RedisNode;
import com.mpush.tools.Jsons;
import com.mpush.tools.redis.RedisRegister;

import java.util.Collections;
import java.util.List;

public class JedisRegisterManager implements RedisRegister {

    private static List<RedisGroup> groups = Lists.newArrayList();

    /**
     * zk 启动的时候需要调用这个
     */
    @Override
    public void init(List<RedisGroup> group) {
        if (group == null || group.isEmpty()) {
            LoggerManage.log(LogType.REDIS, "init redis client error, redis server is none.");
            throw new RuntimeException("init redis client error, redis server is none.");
        }
        groups = group;
        printGroupList();
    }


    @Override
    public List<RedisGroup> getGroupList() {
        return Collections.unmodifiableList(groups);
    }

    private void printGroupList() {
        for (RedisGroup app : groups) {
            LoggerManage.log(LogType.REDIS, Jsons.toJson(app));
        }
    }

    public int groupSize() {
        return groups.size();
    }

    /**
     * 随机获取一个redis 实例
     *
     * @param key
     * @return
     */
    @Override
    public RedisNode randomGetRedisNode(String key) {
        int size = groupSize();
        if (size == 1) return groups.get(0).get(key);
        int i = (int) ((Math.random() % size) * size);
        RedisGroup group = groups.get(i);
        return group.get(key);
    }

    /**
     * 写操作的时候，获取所有redis 实例
     *
     * @param key
     * @return
     */
    @Override
    public List<RedisNode> hashSet(String key) {
        List<RedisNode> nodeList = Lists.newArrayList();
        for (RedisGroup group : groups) {
            RedisNode node = group.get(key);
            nodeList.add(node);
        }
        return nodeList;
    }

}
