package com.shinemo.mpush.ps;

import com.shinemo.mpush.api.PushSender;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ohun on 2016/1/7.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        GatewayClientMain client = new GatewayClientMain();
        client.start();
        Thread.sleep(1000);
        client.send("this a first push", Arrays.asList("user-0", "user-1", "user-2", "user-3", "user-4"),
                new PushSender.Callback() {
                    @Override
                    public void onSuccess(String userId) {
                        System.err.println("push onSuccess userId=" + userId);
                    }

                    @Override
                    public void onFailure(String userId) {
                        System.err.println("push onFailure userId=" + userId);
                    }

                    @Override
                    public void onOffline(String userId) {
                        System.err.println("push onOffline userId=" + userId);
                    }

                    @Override
                    public void onTimeout(String userId) {
                        System.err.println("push onTimeout userId=" + userId);
                    }
                }
        );
        LockSupport.park();
    }
}