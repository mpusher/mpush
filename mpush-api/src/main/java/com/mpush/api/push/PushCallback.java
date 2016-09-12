package com.mpush.api.push;

import com.mpush.api.router.ClientLocation;

import java.util.List;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public interface PushCallback {

    /**
     * 推送成功, 指定用户推送时重写此方法
     *
     * @param userId   成功的用户
     * @param location 用户所在机器
     */
    default void onSuccess(String userId, ClientLocation location) {

    }

    /**
     * 推送失败
     *
     * @param userId   推送用户
     * @param location 用户所在机器
     */
    void onFailure(String userId, ClientLocation location);

    /**
     * 推送用户不在线
     *
     * @param userId   推送用户
     * @param location 用户所在机器
     */
    void onOffline(String userId, ClientLocation location);

    /**
     * 推送超时
     *
     * @param userId   推送用户
     * @param location 用户所在机器
     */
    void onTimeout(String userId, ClientLocation location);

    /**
     * 推送成功, 广播时重写此方法
     *
     * @param userIds 推送成功的用户列表
     */
    default void onSuccess(List<String> userIds) {

    }
}