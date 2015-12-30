package com.shinemo.mpush.tools.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.CloseableUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DistributedQueueTest {

	private ZkConfig zkConfig = new ZkConfig("127.0.0.1:2181", "mpush");
	
	private ZkUtil zkUtil = new ZkUtil(zkConfig);
	
	private static final String PATH = "/example/queue";
	
	@Before
	public void setup(){
		zkUtil.init();
		zkUtil.getClient().getCuratorListenable().addListener(new CuratorListener() {
			@Override
			public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
				System.out.println("CuratorEvent: "
						+ event.getType().name());
			}
		});
	}
	
	@Test
	public void test() throws Exception{
		DistributedQueue<String> queue = null;
		QueueConsumer<String> consumer = createQueueConsumer();
		QueueBuilder<String> builder = QueueBuilder.builder(zkUtil.getClient(),
				consumer, createQueueSerializer(), PATH);
		queue = builder.buildQueue();
		queue.start();
		for (int i = 0; i < 10; i++) {
			queue.put(" test-" + i);
			Thread.sleep((long) (3 * Math.random()));
		}
		
		Thread.sleep(20000);
		
		CloseableUtils.closeQuietly(queue);
	}
	
	@After
	public void close(){
		zkUtil.close();
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
