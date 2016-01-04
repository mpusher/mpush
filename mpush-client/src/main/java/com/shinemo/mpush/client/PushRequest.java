package com.shinemo.mpush.client;

import com.shinemo.mpush.api.PushSender;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushRequest implements PushSender.Callback, Runnable {
    private PushSender.Callback callback;
    private String userId;
    private String content;
    private long timeout;
    private PushClient pushClient;
    private int status = 0;
    private long timeout_;

    public PushRequest(PushClient pushClient) {
        this.pushClient = pushClient;
    }

    public static PushRequest build(PushClient pushClient) {
        return new PushRequest(pushClient);
    }

    public PushRequest setCallback(PushSender.Callback callback) {
        this.callback = callback;
        return this;
    }

    public PushRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public PushRequest setContent(String content) {
        this.content = content;
        return this;
    }

    public PushRequest setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
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
            PushRequestBus.INSTANCE.getExecutor().execute(this);
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

    public void success() {
        onSuccess(userId);
    }

    public void failure() {
        onFailure(userId);
    }

    public void offline() {
        onOffline(userId);
    }

    public void send() {
        this.timeout_ = timeout + System.currentTimeMillis();
        pushClient.send(content, userId, this);
    }

    public void redirect() {
        this.timeout_ = timeout + System.currentTimeMillis();
        pushClient.send(content, userId, this);
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() > timeout_;
    }
}
