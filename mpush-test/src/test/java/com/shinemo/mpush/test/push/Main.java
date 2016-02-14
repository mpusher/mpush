package com.shinemo.mpush.test.push;

import com.shinemo.mpush.api.PushSender;
import com.shinemo.mpush.tools.Jsons;

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
		for (int i = 0; i < 100; i++) {
			PushContent content = new PushContent("msgId_" + (i % 2), "MPush", "this a first push." + i);

			client.send(Jsons.toJson(content), Arrays.asList("user-0", "8"), new PushSender.Callback() {
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
			});
			Thread.sleep(10000);
		}
		LockSupport.park();
	}

}