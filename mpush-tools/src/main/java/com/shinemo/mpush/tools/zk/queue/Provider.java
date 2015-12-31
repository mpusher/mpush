package com.shinemo.mpush.tools.zk.queue;

import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.zk.ZkUtil;

public class Provider<T> extends BaseQueue<T>{
	
	private static final Logger log = LoggerFactory.getLogger(Provider.class);
	
	private DistributedQueue<T> queue = null;
	
	private String path;
	
	
	public Provider(String path,final Class<T> clazz){
		
		QueueBuilder<T> builder = QueueBuilder.builder(ZkUtil.instance.getClient(),
				null, createQueueSerializer(clazz), path).lockPath(path);
		queue = builder.buildQueue();
		this.path = path;
	}
	
	public void start() throws Exception{
		queue.start();
		log.warn("provider start:"+path);
	}
	
	public void put(T item) throws Exception{
		queue.put(item);
		log.warn("provider put:"+item+","+path);
	}
	
}
