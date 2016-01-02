package com.shinemo.mpush.tools.zk.queue;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.zk.consumer.ConsumerCallBack;

public class BaseQueue<T> {
	
	private static final Logger log = LoggerFactory.getLogger(BaseQueue.class);

	public static <T> QueueSerializer<T> createQueueSerializer(final Class<T> clazz) {
		return new QueueSerializer<T>() {

			@Override
			public byte[] serialize(T item) {
				return Jsons.toJson(item).getBytes();
			}

			@Override
			public T deserialize(byte[] bytes) {
				return Jsons.fromJson(bytes, clazz);
			}

		};
	}

	public static <T> QueueConsumer<T> createQueueConsumer(final Class<T> clazz,final ConsumerCallBack<T> callBack) {
		
		return new QueueConsumer<T>() {
			
			@Override
			public void consumeMessage(T message) throws Exception {
				callBack.handler(message);
				log.warn("consume one message:"+message);
			}
			
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				log.warn("connection new state:"+newState.name());
			}
			
		};

	}
	
	
}
