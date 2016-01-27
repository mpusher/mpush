package com.shinemo.mpush.tools.thread;


import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.thread.threadpool.ThreadPool;
import com.shinemo.mpush.tools.thread.threadpool.ThreadPoolContext;
import com.shinemo.mpush.tools.thread.threadpool.cached.CachedThreadPool;
import com.shinemo.mpush.tools.thread.threadpool.cached.CachedThreadPoolContext;
import com.shinemo.mpush.tools.thread.threadpool.fixed.FixedThreadPool;
import com.shinemo.mpush.tools.thread.threadpool.fixed.FixedThreadPoolContext;

public class SyncTest {
	
	final static ThreadPool cachedThreadPool = new CachedThreadPool();
	final static ThreadPool fixedThreadPool = new FixedThreadPool();
	
	// 最小线程1，最大线程2，使用sync blockqueue,线程缓存5分钟
	final static ThreadPoolContext cachedThreadPoolContext = CachedThreadPoolContext.create("cached-test", 1, 2, 60*5);
	
	// 最小线程1，最大线程2，使用LinkedBlockingQueue,线程缓存5分钟
	final static ThreadPoolContext cachedThreadPoolContext2 = CachedThreadPoolContext.create("cached-test2", 1, 2, 60*5,1);
	
	//线程数1，blockqueue大小为2
	final static ThreadPoolContext fixedThreadPoolContext = FixedThreadPoolContext.create("fix-test",1,2);
	
	final static Executor cachedExecutor = cachedThreadPool.getExecutor(cachedThreadPoolContext);
	
	final static Executor fixedExecutor = fixedThreadPool.getExecutor(fixedThreadPoolContext);
	
	final static Executor cachedExecutor2 = cachedThreadPool.getExecutor(cachedThreadPoolContext2);
	
	Worker worker1 = new Worker();
	Worker worker2 = new Worker();
	Worker worker3 = new Worker();
	Worker worker4 = new Worker();
	Worker worker5 = new Worker();
	
	Monitor monitor = new Monitor(fixedExecutor,cachedExecutor);


	@Before
	public void init(){
		monitor.start();
	}
	
	/**
	 * 容量为2，因此最多可以接收三个任务。第四个任务开始抛异常
	 */
	@Test
	public void testFixed(){
		fixedExecutor.execute(worker1);
		fixedExecutor.execute(worker2);
		fixedExecutor.execute(worker3);
		fixedExecutor.execute(worker4);
		fixedExecutor.execute(worker5);
	}
	
	/**
	 * 最小线程1，最大线程2，没有缓冲队列。所以，第三个任务提交的时候，就已经抛错了。
	 */
	@Test
	public void testCached(){
		cachedExecutor.execute(worker1);
		cachedExecutor.execute(worker2);
		cachedExecutor.execute(worker3);
		cachedExecutor.execute(worker4);
		cachedExecutor.execute(worker5);
	}
	
	/**
	 * 最小线程1，最大线程2，缓冲队列长度为1。所以，第四个任务提交的时候，就已经抛错了。
	 */
	@Test
	public void testCached2(){
		cachedExecutor2.execute(worker1);
		cachedExecutor2.execute(worker2);
		cachedExecutor2.execute(worker3);
		cachedExecutor2.execute(worker4);
		cachedExecutor2.execute(worker5);
	}
	
}


class Worker implements Runnable{
	
	private static final Logger log = LoggerFactory.getLogger(Worker.class);
	
	@Override
	public void run() {
		log.warn("start run Worker:"+Thread.currentThread().getName());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		log.warn("end run Worker:"+Thread.currentThread().getName());
	}
}

class Monitor extends Thread{
	
	private static final Logger log = LoggerFactory.getLogger(Monitor.class);
	
	private ThreadPoolExecutor fixedExecutor;
	private ThreadPoolExecutor cachedExecutor;
	
	public Monitor(Executor fixedExecutor,Executor cachedExecutor) {
		this.fixedExecutor = (ThreadPoolExecutor)fixedExecutor;
		this.cachedExecutor = (ThreadPoolExecutor)cachedExecutor;
	}
	
	@Override
	public void run() {
		
		while(true){
			StringBuilder sb = new StringBuilder();
			sb.append("[fixedExecutor]"+"coreThreadNums:" + fixedExecutor.getCorePoolSize() + " maxThreadNums:" + fixedExecutor.getMaximumPoolSize() + " activityThreadNums:"
					+ fixedExecutor.getActiveCount());
			log.error(sb.toString());
			
			StringBuilder sb2 = new StringBuilder();
			sb2.append("[cachedExecutor]"+"coreThreadNums:" + cachedExecutor.getCorePoolSize() + " maxThreadNums:" + cachedExecutor.getMaximumPoolSize() + " activityThreadNums:"
					+ cachedExecutor.getActiveCount());
			log.error(sb2.toString());
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			
		}
		
	}
	
}
