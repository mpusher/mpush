package com.mpush.cache.redis.manager;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mpush.cache.redis.RedisGroup;
import com.mpush.cache.redis.RedisServer;
import com.mpush.tools.Jsons;
import com.mpush.tools.config.CC;
import com.mpush.tools.log.Logs;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKRedisNodeWatcher;
import com.mpush.zk.node.ZKRedisNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.mpush.zk.ZKPath.REDIS_SERVER;

public class ZKRedisClusterManager implements RedisClusterManager {
    public static final ZKRedisClusterManager I = new ZKRedisClusterManager();

    private ZKRedisClusterManager() {
    }

    private final List<RedisGroup> groups = new ArrayList<>();

    /**
     * zk 启动的时候需要调用这个
     */
    @Override
    public void init() {
        Logs.Console.info("begin init redis cluster");
        List<com.mpush.tools.config.data.RedisGroup> groupList = CC.mp.redis.cluster_group;
        if (groupList.size() > 0) {
            if (CC.mp.redis.write_to_zk) {
                register(groupList);
            } else if (!ZKClient.I.isExisted(REDIS_SERVER.getRootPath())) {
                register(groupList);
            } else if (Strings.isNullOrEmpty(ZKClient.I.get(REDIS_SERVER.getRootPath()))) {
                register(groupList);
            }
        }

        ZKRedisNodeWatcher watcher = new ZKRedisNodeWatcher();
        watcher.beginWatch();
        Collection<ZKRedisNode> nodes = watcher.getCache().values();
        if (nodes == null || nodes.isEmpty()) {
            Logs.REDIS.info("init redis client error, redis server is none.");
            throw new RuntimeException("init redis client error, redis server is none.");
        }
        for (ZKRedisNode node : nodes) {
            groups.add(RedisGroup.from(node));
        }
        if (groups.isEmpty()) throw new RuntimeException("init redis sever fail groupList is null");
        Logs.Console.info("init redis cluster success...");
    }

    @Override
    public List<RedisGroup> getGroupList() {
        return Collections.unmodifiableList(groups);
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
    public RedisServer randomGetRedisNode(String key) {
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
    public List<RedisServer> hashSet(String key) {
        List<RedisServer> nodeList = Lists.newArrayList();
        for (RedisGroup group : groups) {
            RedisServer node = group.get(key);
            nodeList.add(node);
        }
        return nodeList;
    }

    private void register(List<com.mpush.tools.config.data.RedisGroup> groupList) {
        String data = Jsons.toJson(groupList);
        ZKClient.I.registerPersist(REDIS_SERVER.getRootPath(), data);
        Logs.Console.info("register redis server group success, group=" + data);
    }

    public void addGroup(RedisGroup group) {
        groups.add(group);
    }
}
