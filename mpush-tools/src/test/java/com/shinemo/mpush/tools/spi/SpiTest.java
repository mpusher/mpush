package com.shinemo.mpush.tools.spi;


import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Ignore;
import org.junit.Test;

import com.shinemo.mpush.tools.spi.test.TestService;


public class SpiTest {
	
	private static Executor pool = Executors.newCachedThreadPool();
	
	@Test
	public void baseTest(){
		TestService testService = ServiceContainer.getInstance(TestService.class);
		System.out.println(testService.sayHi(" huang"));
		
		ServiceContainer.getInstance(TestService.class,"test2");
	}
	
	@Ignore
	@Test
	public void listTest(){
		
		List<TestService> listRet = ServiceContainer.getInstances(TestService.class);
		
		for(TestService test:listRet){
			System.out.println(ToStringBuilder.reflectionToString(test.sayHi(" huang list")));
		}
		
	}
	
	@Ignore
	@Test
	public void mulThreadTest() throws InterruptedException{
		pool.execute(new Worker());
		pool.execute(new Worker());
		pool.execute(new ListWorker());
		pool.execute(new ListWorker());
		Thread.sleep(Integer.MAX_VALUE);
	}
	

	private static final class Worker implements Runnable{
		
		@Override
		public void run() {
			TestService testService = ServiceContainer.getInstance(TestService.class);
			System.out.println(testService.sayHi(" huang")+","+ToStringBuilder.reflectionToString(testService));
		}
		
	}
	
	private static final class ListWorker implements Runnable{
		
		@Override
		public void run() {
			
			List<TestService> listRet = ServiceContainer.getInstances(TestService.class);
			
			for(TestService test:listRet){
				System.out.println(test.sayHi(" huang list")+","+Thread.currentThread().getId()+","+ToStringBuilder.reflectionToString(test));
			}
			
		}
		
	}
	
}
