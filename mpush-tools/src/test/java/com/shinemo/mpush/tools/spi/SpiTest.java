package com.shinemo.mpush.tools.spi;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Test;

import com.shinemo.mpush.tools.spi.test.TestService;


public class SpiTest {
	
	private static Executor pool = Executors.newCachedThreadPool();
	
	@Ignore
	@Test
	public void baseTest(){
		TestService testService = ServiceContainer.getInstance(TestService.class);
		System.out.println(testService.sayHi(" huang"));
	}
	
	@Test
	public void mulThreadTest() throws InterruptedException{
		pool.execute(new Worker());
		pool.execute(new Worker());
		Thread.sleep(Integer.MAX_VALUE);
	}
	

	private static final class Worker implements Runnable{
		
		@Override
		public void run() {
			TestService testService = ServiceContainer.getInstance(TestService.class);
			System.out.println(testService.sayHi(" huang"));
		}
		
	}
	
}
