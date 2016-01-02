package com.shinemo.mpush.tools.zk.queue;

import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.zk.ZkUtil;
import com.shinemo.mpush.tools.zk.consumer.ConsumerCallBack;

public class Consumer<T> extends BaseQueue<T>{
	
	private static final Logger log = LoggerFactory.getLogger(Consumer.class);
	
	private DistributedQueue<T> queue = null;
	private String path;
	
	public Consumer(String path,final Class<T> clazz,final ConsumerCallBack<T> callBack){
		QueueBuilder<T> builder = QueueBuilder.builder(ZkUtil.instance.getClient(),
				createQueueConsumer(clazz,callBack), createQueueSerializer(clazz), path);
		queue = builder.buildQueue();
		this.path = path;
	}
	
	public void start() throws Exception{
		queue.start();
		log.warn("consumer start:"+path);
	}
	
}
