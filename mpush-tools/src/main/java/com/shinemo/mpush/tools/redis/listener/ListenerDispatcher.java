package com.shinemo.mpush.tools.redis.listener;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

public class ListenerDispatcher implements MessageListener {

	private Map<String, MessageListener> holder = Maps.newTreeMap();

	public static ListenerDispatcher instance = new ListenerDispatcher();
	
	private ListenerDispatcher() {
	}

	@Override
	public void onMessage(String channel, String message) {
		Iterator<Map.Entry<String, MessageListener>> it = holder.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, MessageListener> entry = it.next();
			entry.getValue().onMessage(channel, message);
		}
	}


}
