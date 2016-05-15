package com.mpush.boot;

import com.google.common.base.Strings;
import com.mpush.tools.ConsoleLog;
import com.mpush.tools.Jsons;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.redis.RedisGroup;
import com.mpush.tools.redis.manage.RedisManage;
import com.mpush.zk.ZKClient;

import java.util.List;

import static com.mpush.zk.ZKPath.REDIS_SERVER;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class RedisBoot extends BootJob {

    @Override
    public void run() {
        List<RedisGroup> groupList = ConfigCenter.I.redisGroups();
        if (groupList.size() > 0) {
            if (ConfigCenter.I.forceWriteRedisGroupInfo()) {
                register(groupList);
            } else if (!ZKClient.I.isExisted(REDIS_SERVER.getPath())) {
                register(groupList);
            } else if (Strings.isNullOrEmpty(ZKClient.I.get(REDIS_SERVER.getPath()))) {
                register(groupList);
            }
        } else {
            throw new RuntimeException("init redis sever fail groupList is null");
        }

        RedisManage.test(groupList);
        next();
    }

    private void register(List<RedisGroup> groupList) {
        String data = Jsons.toJson(groupList);
        ZKClient.I.registerPersist(REDIS_SERVER.getPath(), data);
        ConsoleLog.i("register redis server group success, group=" + data);
    }
}
