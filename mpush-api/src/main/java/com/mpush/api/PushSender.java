package com.mpush.api;

import java.util.Collection;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public interface PushSender {
    void send(String content, Collection<String> userIds, Callback callback);
    
    void send(String content, String userId, Callback callback);

    interface Callback {
        void onSuccess(String userId);

        void onFailure(String userId);

        void onOffline(String userId);

        void onTimeout(String userId);
    }
}
