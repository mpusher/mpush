package com.mpush.client.push;

import com.mpush.api.push.PushSender;
import com.mpush.api.spi.client.PusherFactory;

/**
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public class PushClientFactory implements PusherFactory {
    private static PushClient CLIENT;

    @Override
    public PushSender get() {
        if (CLIENT == null) {
            synchronized (PushClientFactory.class) {
                CLIENT = new PushClient();
            }
        }
        return CLIENT;
    }
}
