package com.shinemo.mpush.tools.zk.consumer.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.zk.KickConnection;
import com.shinemo.mpush.tools.zk.consumer.ConsumerCallBack;

public class ConsumerKickListener implements ConsumerCallBack<KickConnection>{

	private static final Logger log = LoggerFactory.getLogger(ConsumerKickListener.class);
	
	@Override
	public void handler(KickConnection message) {
		//TODO 删除本地持有的引用
		log.warn("consumer kick:"+ToStringBuilder.reflectionToString(message));
	}

}
