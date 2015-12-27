package com.shinemo.mpush.tools.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory{
	
	private static final AtomicInteger poolNum = new AtomicInteger(1);
	
	private final AtomicInteger threadNum = new AtomicInteger(1);
	
	private final ThreadGroup group;
	private final String namePre;
	private final boolean isDaemon;

	public NamedThreadFactory(){
		this("pool");
	}
	
	public NamedThreadFactory(String prefix){
		this(prefix,true);
	}
	
	public NamedThreadFactory(String prefix,boolean daemon) {
		SecurityManager manager = System.getSecurityManager();
		if(manager!=null){
			group = manager.getThreadGroup();
		}else{
			group = Thread.currentThread().getThreadGroup();
		}
		isDaemon = daemon;
		namePre = prefix+"-"+poolNum.getAndIncrement()+"-thread-";
	}
	
	/**
     * stackSize - 新线程的预期堆栈大小，为零时表示忽略该参数
	 */
	@Override
	public Thread newThread(Runnable runnable) {
		Thread t = new Thread(group, runnable,namePre+threadNum.getAndIncrement(),0);
		t.setContextClassLoader(NamedThreadFactory.class.getClassLoader());
		t.setPriority(Thread.MAX_PRIORITY);
		t.setDaemon(isDaemon);
		return t;
	}

}
