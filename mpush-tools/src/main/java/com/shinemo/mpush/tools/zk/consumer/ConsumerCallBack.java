package com.shinemo.mpush.tools.zk.consumer;

public interface ConsumerCallBack<T> {
	
	public void handler(T message);

}
