/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.common.user;

import com.mpush.api.Constants;
import com.mpush.api.router.ClientLocation;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.api.spi.common.CacheManagerFactory;
import com.mpush.api.spi.common.MQClient;
import com.mpush.api.spi.common.MQClientFactory;
import com.mpush.common.CacheKeys;
import com.mpush.common.druid.MysqlConnecter;
import com.mpush.common.mysql.DateUtils;
import com.mpush.common.router.MQKickRemoteMsg;
import com.mpush.common.router.RemoteRouter;
import com.mpush.common.router.RemoteRouterManager;
import com.mpush.tools.config.ConfigTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

//查询使用
public final class UserManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

    private final String onlineUserListKey = CacheKeys.getOnlineUserListKey(ConfigTools.getPublicIp());

    private final CacheManager cacheManager = CacheManagerFactory.create();

    private final MQClient mqClient = MQClientFactory.create();

    private final RemoteRouterManager remoteRouterManager;

    public UserManager(RemoteRouterManager remoteRouterManager) {
        this.remoteRouterManager = remoteRouterManager;
    }

    public void kickUser(String userId) {
        kickUser(userId, -1);
    }

    public void kickUser(String userId, int clientType) {
        Set<RemoteRouter> remoteRouters = remoteRouterManager.lookupAll(userId);
        if (remoteRouters != null) {
            for (RemoteRouter remoteRouter : remoteRouters) {
                ClientLocation location = remoteRouter.getRouteValue();
                if (clientType == -1 || location.getClientType() == clientType) {
                    MQKickRemoteMsg message = new MQKickRemoteMsg()
                            .setUserId(userId)
                            .setClientType(location.getClientType())
                            .setConnId(location.getConnId())
                            .setDeviceId(location.getDeviceId())
                            .setTargetServer(location.getHost())
                            .setTargetPort(location.getPort());
                    mqClient.publish(Constants.getKickChannel(location.getHostAndPort()), message);
                }
            }
        }
    }

    public void clearUserOnlineData() {
        cacheManager.del(onlineUserListKey);
    }

    public void addToOnlineList(String userId) {
        cacheManager.zAdd(onlineUserListKey, userId);
        System.out.println("用户上线在这里" + userId);
        MysqlConnecter mc = new MysqlConnecter();
        mc.update("update m_user set is_online=2 where device_id=\"" + userId + "\"");

        //上线后，修改离线时间的值为0

        String user_id = mc.selectOne("select user_id from m_user where device_id=\"" + userId + "\"");
        System.out.println("-----用户上线后，查询出的用户id--------"+user_id);
        if (StringUtils.isNotBlank(user_id)) {
            String result = mc.selectOne("select user_last_time_id from m_user_online_time where user_id=\"" + user_id + "\"");
            if (result != null) {
                mc.update("update m_user_online_time set create_time=0 where user_last_time_id=\"" + result + "\"");
            } else {
                mc.update("insert into m_user_online_time(user_id,create_time) values(\"" + user_id + "\",\"0\")");
            }

            LOGGER.info("user online {}", userId);
        }
    }

    public void remFormOnlineList(String userId) {
        cacheManager.zRem(onlineUserListKey, userId);
        System.out.println("用户掉线在这里" + userId);
        MysqlConnecter mc = new MysqlConnecter();
        mc.update("update m_user set is_online=1 where device_id=\"" + userId + "\"");

        // 用户离线，确认离线时间
        String user_id = mc.selectOne("select user_id from m_user where device_id=\"" + userId + "\"");
        System.out.println("-----用户掉线查询出的用户id--------" + user_id);
        if (StringUtils.isNotBlank(user_id)) {
            String result = mc.selectOne("select user_last_time_id from m_user_online_time where user_id=\"" + user_id + "\"");
            DateUtils dateUtils = new DateUtils();
            String now = dateUtils.getNow(dateUtils.FORMAT_LONG);
            if (result != null) {
                mc.update("update m_user_online_time set create_time=\"" + now + "\" where user_last_time_id=\"" + result + "\"");
            } else {
                mc.update("insert into m_user_online_time(user_id,create_time) values(\"" + user_id + "\",\"" + now + "\")");
            }

            LOGGER.info("user offline {}", userId);
        }
    }

    //在线用户数量
    public long getOnlineUserNum() {
        Long value = cacheManager.zCard(onlineUserListKey);
        return value == null ? 0 : value;
    }

    //在线用户数量
    public long getOnlineUserNum(String publicIP) {
        String online_key = CacheKeys.getOnlineUserListKey(publicIP);
        Long value = cacheManager.zCard(online_key);
        return value == null ? 0 : value;
    }

    //在线用户列表
    public List<String> getOnlineUserList(String publicIP, int start, int end) {
        String key = CacheKeys.getOnlineUserListKey(publicIP);
        return cacheManager.zrange(key, start, end, String.class);
    }
}
