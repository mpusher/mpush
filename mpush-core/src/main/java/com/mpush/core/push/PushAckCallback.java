package com.mpush.core.push;

import com.mpush.api.spi.push.IPushMessage;
import com.mpush.core.ack.AckCallback;
import com.mpush.core.ack.AckTask;
import com.mpush.tools.log.Logs;

public class PushAckCallback implements AckCallback {
    private final IPushMessage message;

    public PushAckCallback(IPushMessage message) {
        this.message = message;
    }

    @Override
    public void onSuccess(AckTask task) {
        PushCenter.I.getPushListener().onAckSuccess(message);
        Logs.PUSH.info("receive client ack success, task={}", task);
    }

    @Override
    public void onTimeout(AckTask task) {
        PushCenter.I.getPushListener().onAckTimeout(message);
        Logs.PUSH.warn("receive client ack timeout, task={}", task);
    }
}