package com.mpush.boot;

import com.google.common.base.Strings;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.tools.Jsons;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.redis.RedisGroup;
import com.mpush.tools.redis.manage.RedisManage;

import java.util.List;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class RedisBoot extends BootJob {

    @Override
    public void run() {
        List<RedisGroup> groupList = ConfigCenter.holder.redisGroups();
        if (groupList.isEmpty()) throw new RuntimeException("init redis sever ex");
        boolean exist = ZKClient.I.isExisted(ZKPath.REDIS_SERVER.getPath());
        String rawGroup = ZKClient.I.get(ZKPath.REDIS_SERVER.getPath());
        if (!exist || Strings.isNullOrEmpty(rawGroup)) {
            ZKClient.I.registerPersist(ZKPath.REDIS_SERVER.getPath(), Jsons.toJson(groupList));
        }
        //强刷
        boolean forceWriteRedisGroupInfo = ConfigCenter.holder.forceWriteRedisGroupInfo();
        if (forceWriteRedisGroupInfo) {
            ZKClient.I.registerPersist(ZKPath.REDIS_SERVER.getPath(), Jsons.toJson(groupList));
        }
        RedisManage.test(groupList);
        next();
    }
}
