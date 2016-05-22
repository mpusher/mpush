package com.mpush.api.push;

import com.mpush.api.Service;
import com.mpush.api.spi.SpiLoader;
import com.mpush.api.spi.client.PusherFactory;

import java.util.Collection;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public interface PushSender extends Service {
    PusherFactory factory = SpiLoader.load(PusherFactory.class);

    void send(String content, Collection<String> userIds, Callback callback);

    void send(String content, String userId, Callback callback);

    void start();

    void stop();

    interface Callback {
        void onSuccess(String userId);

        void onFailure(String userId);

        void onOffline(String userId);

        void onTimeout(String userId);
    }
}
