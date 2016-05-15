package com.mpush.api;

/**
 * Created by ohun on 2015/12/24.
 *
 * @author ohun@live.cn
 */
public interface Server {

    void start(Listener listener);

    void stop(Listener listener);

    void init();

    boolean isRunning();

    interface Listener {
        void onSuccess(int port);

        void onFailure(Throwable cause);
    }
}
