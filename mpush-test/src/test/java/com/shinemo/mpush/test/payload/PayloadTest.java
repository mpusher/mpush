package com.shinemo.mpush.test.payload;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.shinemo.mpush.api.PushContent;
import com.shinemo.mpush.api.PushContent.PushType;
import com.shinemo.mpush.api.payload.CustomPushPayload;
import com.shinemo.mpush.api.payload.NotificationPushPayload;
import com.shinemo.mpush.api.payload.PayloadFactory;
import com.shinemo.mpush.api.payload.PayloadFactory.MessageType;
import com.shinemo.mpush.tools.Jsons;

public class PayloadTest {
	
	@Test
	public void customTest(){
		
		Map<String, String> map = Maps.newHashMap();
		map.put("key1", "value1");
		map.put("key2", "value2");
		
		CustomPushPayload customPayload = PayloadFactory.create(MessageType.CUSTOM);
		customPayload.putAll(map);
		
		PushContent pushContent = PushContent.build(PushType.MESSAGE, Jsons.toJson(customPayload));
		
		System.out.println(Jsons.toJson(pushContent));
	}
	
	@Test
	public void notificationTest(){
		NotificationPushPayload payload = PayloadFactory.create(MessageType.NOTIFICATION);
		payload.setContent("content");
		payload.setTitle("title");
		PushContent pushContent = PushContent.build(PushType.NOTIFICATION, Jsons.toJson(payload));
		
		System.out.println(Jsons.toJson(pushContent));
	}

}
