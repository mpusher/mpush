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

package com.mpush.client.push;

import com.mpush.api.MPushContext;
import com.mpush.api.push.PushContext;
import com.mpush.api.push.PushException;
import com.mpush.api.push.PushResult;
import com.mpush.api.push.PushSender;
import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.api.spi.common.CacheManagerFactory;
import com.mpush.api.spi.common.ServiceDiscoveryFactory;
import com.mpush.api.utils.SetUtil;
import com.mpush.client.MPushClient;
import com.mpush.client.gateway.connection.GatewayConnectionFactory;
import com.mpush.common.CacheKeys;
import com.mpush.common.router.CachedRemoteRouterManager;
import com.mpush.common.router.RemoteRouter;
import com.mpush.tools.StringUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.FutureTask;

/**
 * 推送客户端
 */
public final class PushClient extends BaseService implements PushSender {

    private MPushClient mPushClient;

    private PushRequestBus pushRequestBus;

    private CachedRemoteRouterManager cachedRemoteRouterManager;

    private GatewayConnectionFactory gatewayConnectionFactory;

    private CacheManager cacheManager;

    /**
     * 发送推送请求
     * @param ctx
     * @return
     */
    private FutureTask<PushResult> send0(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return PushRequest.build(mPushClient, ctx).broadcast();
        } else {
            Set<RemoteRouter> remoteRouters = cachedRemoteRouterManager.lookupAll(ctx.getUserId());
            if (remoteRouters == null || remoteRouters.isEmpty()) {
                // 用户离线
                return PushRequest.build(mPushClient, ctx).onOffline();
            }
            FutureTask<PushResult> task = null;
            for (RemoteRouter remoteRouter : remoteRouters) {
                task = PushRequest.build(mPushClient, ctx).send(remoteRouter);
            }
            return task;
        }
    }

    //@Override
    public FutureTask<PushResult> send(PushContext ctx) {
        if (ctx.isBroadcast()) {
            // 广播
            return send0(ctx.setUserId(null));
        } else if (ctx.getUserId() != null) {
            // 按用户id推送
            return send0(ctx);
        } else if (ctx.getUserIds() != null) {
            // 按多个用户id推送
            FutureTask<PushResult> task = null;
            for (String userId : ctx.getUserIds()) {
                task = send0(ctx.setUserId(userId));
            }
            return task;
        } else if (ctx.getAliasSet() != null) {
            // 按多个别名推送
            // 通过别名查找对应的用户id
            Set<String> userIdSet = new HashSet<>();
            for(String alias : ctx.getAliasSet()){
                String userId = cacheManager.hget(CacheKeys.ALIAS_INFO_KEY, alias, String.class);
                if(userId != null){
                    userIdSet.add(userId);
                }
            }
            FutureTask<PushResult> task = null;
            for (String userId : userIdSet) {
                task = send0(ctx.setUserId(userId));
            }
            userIdSet = null;
            return task;
        } else if (ctx.getTags() != null) {
            // 按多个标签推送
            // 通过标签查找对应的用户id
            Set<String> userIdSet = new HashSet<>();
            for(String tag : ctx.getTags()){
                String[] userIds = cacheManager.hget(CacheKeys.TAGS_INFO_KEY, tag, String[].class);
                if(userIds != null && userIds.length>0){
                    userIdSet.addAll(SetUtil.toSet(userIds));
                }
            }
            FutureTask<PushResult> task = null;
            for (String userId : userIdSet) {
                task = send0(ctx.setUserId(userId));
            }
            userIdSet = null;
            return task;
        } else {
            throw new PushException("param error.");
        }
    }

    @Override
    public FutureTask<PushResult> sendByUserId(PushContext context) {
        if(context.getUserId() == null
                || context.getUserIds() != null
                || context.getAliasSet() != null
                || context.getTags() != null){
            throw new PushException("param error.");
        }
        if(!StringUtil.verifyUserId(context.getUserId())){
            throw new PushException("param error.");
        }
        return send(context);
    }
    @Override
    public FutureTask<PushResult> sendByUserIds(PushContext context) {
        if(context.getUserIds() == null
                || context.getUserIds().isEmpty()
                || context.getUserId() != null
                || context.getAliasSet() != null
                || context.getTags() != null){
            throw new PushException("param error.");
        }
        for(String userId : context.getUserIds()){
            if(!StringUtil.verifyUserId(userId)){
                throw new PushException("param error.");
            }
        }
        return send(context);
    }
    @Override
    public FutureTask<PushResult> sendByAlias(PushContext context) {
        if(context.getAliasSet() == null
                || context.getAliasSet().isEmpty()
                || context.getUserId() != null
                || context.getUserIds() != null
                || context.getTags() != null){
            throw new PushException("param error.");
        }
        for(String alias : context.getAliasSet()){
            if(!StringUtil.verifyAlias(alias)){
                throw new PushException("param error.");
            }
        }
        return send(context);
    }
    @Override
    public FutureTask<PushResult> sendByTags(PushContext context) {
        if(context.getTags() == null
                || context.getTags().isEmpty()
                || context.getUserId() != null
                || context.getUserIds() != null
                || context.getAliasSet() != null){
            throw new PushException("param error.");
        }
        for(String tags : context.getTags()){
            if(!StringUtil.verifyTags(tags)){
                throw new PushException("param error.");
            }
        }
        return send(context);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        if (mPushClient == null) {
            mPushClient = new MPushClient();
        }

        pushRequestBus = mPushClient.getPushRequestBus();
        cachedRemoteRouterManager = mPushClient.getCachedRemoteRouterManager();
        gatewayConnectionFactory = mPushClient.getGatewayConnectionFactory();

        ServiceDiscoveryFactory.create().syncStart();
        CacheManagerFactory.create().init();
        pushRequestBus.syncStart();
        gatewayConnectionFactory.start(listener);
        cacheManager = CacheManagerFactory.create();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        ServiceDiscoveryFactory.create().syncStop();
        CacheManagerFactory.create().destroy();
        pushRequestBus.syncStop();
        gatewayConnectionFactory.stop(listener);
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    @Override
    public void setMPushContext(MPushContext context) {
        this.mPushClient = ((MPushClient) context);
    }
}
