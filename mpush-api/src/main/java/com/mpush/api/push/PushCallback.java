package com.mpush.api.push;

import com.mpush.api.router.ClientLocation;

import java.util.List;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public interface PushCallback {

    default void onResult(PushResult result) {
        switch (result.resultCode) {
            case PushResult.CODE_SUCCESS:
                onSuccess(result.userId, result.location);
                break;
            case PushResult.CODE_FAILURE:
                onFailure(result.userId, result.location);
                break;
            case PushResult.CODE_OFFLINE:
                onOffline(result.userId, result.location);
                break;
            case PushResult.CODE_TIMEOUT:
                onTimeout(result.userId, result.location);
                break;
        }
    }

    /**
     * 推送成功, 指定用户推送时重写此方法
     *
     * @param userId   成功的用户, 如果是广播, 值为空
     * @param location 用户所在机器, 如果是广播, 值为空
     */
    default void onSuccess(String userId, ClientLocation location) {
    }

    /**
     * 推送失败
     *
     * @param userId   推送用户
     * @param location 用户所在机器
     */
    default void onFailure(String userId, ClientLocation location) {
    }

    /**
     * 推送用户不在线
     *
     * @param userId   推送用户
     * @param location 用户所在机器
     */
    default void onOffline(String userId, ClientLocation location) {
    }

    /**
     * 推送超时
     *
     * @param userId   推送用户
     * @param location 用户所在机器
     */
    default void onTimeout(String userId, ClientLocation location) {
    }
}