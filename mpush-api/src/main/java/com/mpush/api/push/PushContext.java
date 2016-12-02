/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.api.push;

import com.mpush.api.Constants;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ohun on 16/9/8.
 *
 * @author ohun@live.cn (夜色)
 */
public class PushContext {
    /**
     * 待推送的内容
     */
    private byte[] context;

    /**
     * 待推送的消息
     */
    private PushMsg pushMsg;

    /**
     * 目标用户
     */
    private String userId;

    /**
     * 目标用户,批量
     */
    private List<String> userIds;

    /**
     * 消息ack模式
     */
    private AckModel ackModel = AckModel.NO_ACK;

    /**
     * 推送成功后的回调
     */
    private PushCallback callback;

    /**
     * 推送超时时间
     */
    private int timeout = 3000;

    //================================broadcast=====================================//

    /**
     * 全网广播在线用户
     */
    private boolean broadcast = false;

    /**
     * 用户标签过滤,目前只有include, 后续会增加exclude
     */
    private Set<String> tags;

    /**
     * 条件表达式, 满足条件的用户会被推送，目前支持的脚本语言为js
     * 可以使用的参数为 userId,tags,clientVersion,osName,osVersion
     * 比如 :
     * 灰度：userId % 100 < 20
     * 包含test标签：tags!=null && tags.indexOf("test")!=-1
     * 判断客户端版本号：clientVersion.indexOf("android")!=-1 && clientVersion.replace(/[^\d]/g,"") > 20
     * 等等
     */
    private String condition;

    /**
     * 广播推送的时候可以考虑生成一个ID, 便于控制任务。
     */
    private String taskId;

    public PushContext(byte[] context) {
        this.context = context;
    }

    public PushContext(PushMsg pushMsg) {
        this.pushMsg = pushMsg;
    }

    public static PushContext build(String msg) {
        return new PushContext(msg.getBytes(Constants.UTF_8));
    }

    public static PushContext build(PushMsg msg) {
        return new PushContext(msg);
    }

    public static String genTaskId() {
        return UUID.randomUUID().toString();
    }

    public byte[] getContext() {
        return context;
    }

    public String getUserId() {
        return userId;
    }

    public PushContext setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public PushContext setUserIds(List<String> userIds) {
        this.userIds = userIds;
        return this;
    }

    public AckModel getAckModel() {
        return ackModel;
    }

    public PushContext setAckModel(AckModel ackModel) {
        this.ackModel = ackModel;
        return this;
    }

    public PushCallback getCallback() {
        return callback;
    }

    public PushContext setCallback(PushCallback callback) {
        this.callback = callback;
        return this;
    }

    public PushMsg getPushMsg() {
        return pushMsg;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public PushContext setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public PushContext setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Set<String> getTags() {
        return tags;
    }

    public PushContext setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public String getCondition() {
        return condition;
    }

    public PushContext setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public PushContext setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }
}
