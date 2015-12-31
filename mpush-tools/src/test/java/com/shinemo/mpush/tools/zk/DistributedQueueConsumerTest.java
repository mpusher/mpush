package com.shinemo.mpush.tools.zk;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shinemo.mpush.tools.zk.manage.ServerManage;
import com.shinemo.mpush.tools.zk.queue.Consumer;

public class DistributedQueueConsumerTest {

	private ServerApp app = new ServerApp("10.1.10.65", "3000");

	private ServerManage manage = new ServerManage(app);

	@Before
	public void setup() {
		manage.start();
	}

	@Test
	public void test() throws Exception{
		
		Consumer<ServerApp> consumer = new Consumer<ServerApp>(PathEnum.CONNECTION_SERVER_KICK.getPathByIp(app.getIp()), ServerApp.class);
		consumer.start();
		
	}

	@After
	public void close() {
		manage.close();
	}
	

	
}
