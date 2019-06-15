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

package com.mpush.core.router;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.mpush.api.Constants;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.UserOfflineEvent;
import com.mpush.api.event.UserOnlineEvent;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.api.spi.common.CacheManagerFactory;
import com.mpush.api.spi.common.MQClient;
import com.mpush.api.spi.common.MQClientFactory;
import com.mpush.api.utils.ArrayUtil;
import com.mpush.api.utils.SetUtil;
import com.mpush.common.CacheKeys;
import com.mpush.common.message.PushMessage;
import com.mpush.common.router.RemoteRouterManager;
import com.mpush.common.user.UserManager;
import com.mpush.tools.event.EventConsumer;

import java.util.HashSet;
import java.util.Set;

import static com.mpush.api.event.Topics.OFFLINE_CHANNEL;
import static com.mpush.api.event.Topics.ONLINE_CHANNEL;

/**
 * Created by ohun on 2015/12/23.
 *
 * 用户事件消费者
 *
 * @author ohun@live.cn
 */
public final class UserEventConsumer extends EventConsumer {

    private final MQClient mqClient = MQClientFactory.create();

    private final UserManager userManager;

    private final CacheManager cacheManager;

    public UserEventConsumer(RemoteRouterManager remoteRouterManager) {
        this.userManager = new UserManager(remoteRouterManager);
        this.cacheManager = CacheManagerFactory.create();
    }

    /**
     * 在线事件处理
     * @param event
     */
    @Subscribe
    @AllowConcurrentEvents
    void on(UserOnlineEvent event) {
        // 添加到本地在线列表
        userManager.addToOnlineList(event.getUserId());
        // 广播用户上线
        mqClient.publish(ONLINE_CHANNEL, event.getUserId());

        // 保存用户id、别名、标签、登录设备信息
        saveUserInfo(event.getConnection().getSessionContext());

        // 将离线信息推送给用户
        sendOfflineMsg(event);
    }

    /**
     * 保存用户id、别名、标签、登录设备信息
     * @param sessionContext
     */
    void saveUserInfo(SessionContext sessionContext){
        String userId = sessionContext.userId;
        String alias = sessionContext.alias;
        String tags = sessionContext.tags;

        String deviceId = sessionContext.deviceId;
        String osName = sessionContext.osName;
        String osVersion = sessionContext.osVersion;

        String deviceInfo = deviceId+","+osName+","+osVersion;

        String key = CacheKeys.getUserInfoKey(userId);

        String cachedAliasUserId = cacheManager.hget(CacheKeys.ALIAS_INFO_KEY_PREFIX, alias, String.class);
        if(cachedAliasUserId!=null && !cachedAliasUserId.equals(userId)){
            // 该别名已存在且用户id不相等，设置失败
        }else{
            cacheManager.hset(key, CacheKeys.USER_INFO_FIELD_ALIAS, alias);
            cacheManager.hset(CacheKeys.ALIAS_INFO_KEY_PREFIX, alias, userId);
        }
        String oldTags = cacheManager.hget(key, CacheKeys.USER_INFO_FIELD_TAGS, String.class);
        if(oldTags!=null && !oldTags.equals(tags)){
            cacheManager.hset(key, CacheKeys.USER_INFO_FIELD_TAGS, tags);
        }

        Set<String> oldTagsSet = oldTags!=null ? SetUtil.toSet(oldTags) : new HashSet<>();
        Set<String> newTagsSet = tags!=null ? SetUtil.toSet(tags) : new HashSet<>();
        // 交集
        Set<String> retainTagsSet = new HashSet<>();
        retainTagsSet.addAll(oldTagsSet);
        retainTagsSet.retainAll(newTagsSet);
        // 差集
        oldTagsSet.removeAll(retainTagsSet);
        // 差集
        newTagsSet.removeAll(retainTagsSet);
        if(oldTagsSet!=null && !oldTagsSet.isEmpty()){
            // 从旧标签删除本用户id
            for(String tag : oldTagsSet){
                String[] cachedTagsUserId = cacheManager.hget(CacheKeys.TAGS_INFO_KEY_PREFIX, tag, String[].class);
                cachedTagsUserId = ArrayUtil.removeArr(cachedTagsUserId, userId);
                cacheManager.hset(CacheKeys.TAGS_INFO_KEY_PREFIX, tag, cachedTagsUserId);
            }
        }
        if(newTagsSet!=null && !newTagsSet.isEmpty()){
            // 将本用户id添加到新标签
            for(String tag : newTagsSet){
                String[] cachedTagsUserId = cacheManager.hget(CacheKeys.TAGS_INFO_KEY_PREFIX, tag, String[].class);
                cachedTagsUserId = ArrayUtil.addArr(cachedTagsUserId, userId);
                cacheManager.hset(CacheKeys.TAGS_INFO_KEY_PREFIX, tag, cachedTagsUserId);
            }
        }
        retainTagsSet = null;
        oldTagsSet = null;
        newTagsSet = null;

        String[] devices = cacheManager.hget(key, CacheKeys.USER_INFO_FIELD_DEVICES, String[].class);
        String[] newDevices = ArrayUtil.addArr(devices, deviceInfo);
        if(devices!=newDevices){
            // 将用户在线设备添加到缓存
            cacheManager.hset(key, CacheKeys.USER_INFO_FIELD_DEVICES, newDevices);
        }
    }

    /**
     * 将离线信息推送给用户
     */
    void sendOfflineMsg(UserOnlineEvent event){
        String key = CacheKeys.getUserInfoKey(event.getUserId());
        String[] msgs = cacheManager.hget(key, CacheKeys.USER_INFO_FIELD_MSG, String[].class);
        for(String msgId : msgs){
            String json = cacheManager.get(msgId, String.class);
            byte[] content = json.getBytes(Constants.UTF_8);
            PushMessage pushMessage = PushMessage.build(event.getConnection()).setContent(content);
            pushMessage.send();
        }
        cacheManager.hdel(key, CacheKeys.USER_INFO_FIELD_MSG);
    }

    /**
     * 离线事件处理
     * @param event
     */
    @Subscribe
    @AllowConcurrentEvents
    void on(UserOfflineEvent event) {
        // 从本地在线列表删除
        userManager.remFormOnlineList(event.getUserId());
        // 广播用户离线
        mqClient.publish(OFFLINE_CHANNEL, event.getUserId());

        // 将用户离线设备从缓存删除
        SessionContext sessionContext = event.getConnection().getSessionContext();
        String userId = sessionContext.userId;

        String deviceId = sessionContext.deviceId;
        String osName = sessionContext.osName;
        String osVersion = sessionContext.osVersion;

        String deviceInfo = deviceId+","+osName+","+osVersion;

        String key = CacheKeys.getUserInfoKey(userId);
        String[] devices = cacheManager.hget(key, CacheKeys.USER_INFO_FIELD_DEVICES, String[].class);
        String[] newDevices = ArrayUtil.removeArr(devices, deviceInfo);
        if(devices!=newDevices){
            cacheManager.hset(key, CacheKeys.USER_INFO_FIELD_DEVICES, newDevices);
        }
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
