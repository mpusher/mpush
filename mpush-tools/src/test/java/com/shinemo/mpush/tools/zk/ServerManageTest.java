package com.shinemo.mpush.tools.zk;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.zk.manage.ServerManage;

public class ServerManageTest {
	
	private static Executor executor = Executors.newCachedThreadPool();
	
	@Test
	public void testMulThread() throws InterruptedException{
		CountDownLatch latch = new CountDownLatch(1);
		for(int i = 1;i<=10;i++){
			executor.execute(new Worker("192.168.1."+i, latch));
		}
		latch.countDown();
		
		Thread.sleep(Integer.MAX_VALUE);
	}
	
	
	@Test
	public void testUpdate(){
		ServerManage manage = ServerManage.instance;
		manage.start();
		
	}
	
	@Test
	public void testServerManageStart(){
		ServerManage manage = ServerManage.instance;
		manage.start();
	}
	
	
	private static class Worker implements Runnable{

		private static final Logger log = LoggerFactory.getLogger(Worker.class);
		
		private final String ip;
		private final CountDownLatch latch;
		
		public Worker(String ip, CountDownLatch latch) {
			this.ip = ip;
			this.latch = latch;
		}

		@Override
		public void run() {
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.warn("start init "+ip);
			ServerManage manage = ServerManage.instance;
			manage.start();
			log.warn("end init "+ip);
		}
		
	}

}
