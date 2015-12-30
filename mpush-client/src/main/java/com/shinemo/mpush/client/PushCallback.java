package com.shinemo.mpush.client;

import com.shinemo.mpush.api.PushSender;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushCallback implements PushSender.Callback, Runnable {
    private final PushSender.Callback callback;
    private final String userId;
    private int status = 0;
    private long timeout;

    public PushCallback(PushSender.Callback callback, String userId, int timeout) {
        this.callback = callback;
        this.userId = userId;
        this.timeout = timeout + System.currentTimeMillis();
    }

    @Override
    public void onSuccess(String userId) {
        submit(1);
    }

    @Override
    public void onFailure(String userId) {
        submit(2);
    }

    @Override
    public void onOffline(String userId) {
        submit(3);
    }

    @Override
    public void onTimeout(String userId) {
        submit(4);
    }

    private void submit(int status) {
        this.status = status;
        if (callback != null) {
            PushCallbackBus.INSTANCE.getExecutor().execute(this);
        } else {

        }
    }

    @Override
    public void run() {
        switch (status) {
            case 1:
                callback.onSuccess(userId);
            case 2:
                callback.onFailure(userId);
            case 3:
                callback.onOffline(userId);
            case 4:
                callback.onTimeout(userId);
        }
    }

    public void timeout() {
        onTimeout(userId);
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() > timeout;
    }
}
