package com.mpush.api;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public interface Service {

    void start(Listener listener);

    void stop(Listener listener);

    void init();

    boolean isRunning();

    interface Listener {
        void onSuccess(Object... args);

        void onFailure(Throwable cause);
    }
}
