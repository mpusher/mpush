package com.shinemo.mpush.tools.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.CloseableUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

public class DistributedQueueConsumerTest {

	private ServerManage manage = new ServerManage();
	
	@Before
	public void setup(){
	}
	
	@Test
	public void test() throws Exception{
		
		QueueConsumer<String> consumer = createQueueConsumer();
		
	}
	
	@After
	public void close(){
		manage.close();
	}
	
	
	private static QueueSerializer<String> createQueueSerializer() {
		return new QueueSerializer<String>() {

			@Override
			public byte[] serialize(String item) {
				return item.getBytes();
			}

			@Override
			public String deserialize(byte[] bytes) {
				return new String(bytes);
			}

		};
	}

	private static QueueConsumer<String> createQueueConsumer() {
		
		return new QueueConsumer<String>() {
			
			@Override
			public void consumeMessage(String message) throws Exception {
				System.out.println("consume one message: " + message);
			}
			
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				System.out.println("connection new state: " + newState.name());
			}
		};

	}
	
}
