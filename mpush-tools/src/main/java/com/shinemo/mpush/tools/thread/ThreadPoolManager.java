package com.shinemo.mpush.tools.thread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.JVMUtil;


public class ThreadPoolManager {
	
	private static final Logger log = LoggerFactory.getLogger(ThreadPoolManager.class);
	
	private static final long keepAliveTime = 300L;
	
	private final RejectedExecutionHandler handler = new IgnoreRunsPolicy();
	
	private final ThreadPoolExecutor defaultPoolExecutor;
	
	private final Map<String, ThreadPoolExecutor> poolCache = new HashMap<String, ThreadPoolExecutor>();
	
	public ThreadPoolManager(int corePoolSize,int maxPoolSize,int queueSize){
		final BlockingQueue<Runnable> workQueue = new SynchronousQueue<Runnable>();
		final ThreadFactory threadFactory = new NamedThreadFactory(ThreadNameSpace.NETTY_WORKER);
		defaultPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, 
				TimeUnit.SECONDS, workQueue,threadFactory,handler);
	}
	
	
	private static class IgnoreRunsPolicy implements RejectedExecutionHandler{
		
		private final static Logger log = LoggerFactory.getLogger(IgnoreRunsPolicy.class);

		private volatile boolean dump = false;
		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			dumpJVMInfo();
			throw new RejectedExecutionException();
		}
		
		private void dumpJVMInfo(){
			if (!dump) {
				dump = true;
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        String logPath = Constants.JVM_LOG_PATH;
                        FileOutputStream jstackStream = null;
                        try {
                            jstackStream = new FileOutputStream(new File(logPath, "jstack.log"));
                            JVMUtil.jstack(jstackStream);
                        } catch (FileNotFoundException e) {
                        	log.error("", "Dump JVM cache Error!", e);
                        } catch (Throwable t) {
                        	log.error("", "Dump JVM cache Error!", t);
                        } finally {
                            if (jstackStream != null) {
                                try {
                                    jstackStream.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    }
                });

            }
		}
		
	}
	

    public void allocThreadPool(final String serviceUniqueName, int corePoolSize, int maximumPoolSize)
            throws Exception {
        if (poolCache.containsKey(serviceUniqueName)) { // 对同一个服务重复分配线程池时，抛出异常
            throw new Exception(MessageFormat.format(
                    "[ThreadPool Manager] Duplicated thread pool allocation request for service [{0}].",
                    new Object[] { serviceUniqueName }));
        }

        if (defaultPoolExecutor == null || defaultPoolExecutor.isShutdown()) { // 线程池已关闭
            throw new Exception(MessageFormat.format(
                    "[ThreadPool Manager] Can not allocate thread pool for service [{0}].",
                    new Object[] { serviceUniqueName }));
        }

        int balance = defaultPoolExecutor.getMaximumPoolSize(); // 剩余线程数量
        // 剩余线程数量小于申请的线程数量
        if (balance < maximumPoolSize) {
            throw new Exception(
                            MessageFormat
                                    .format("[ThreadPool Manager] Thread pool allocated failed for service [{0}]: balance [{1}] require [{2}].",
                                            new Object[] { serviceUniqueName, balance, maximumPoolSize }));
        }

        ThreadFactory threadFactory = new NamedThreadFactory(
               ThreadNameSpace.getUniqueName(serviceUniqueName));
        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                    TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory, handler);
            poolCache.put(serviceUniqueName, executor);
        } catch (Exception e) {
            throw new Exception("[ThreadPool Manager] Thread pool allocated failed!", e);
        }

        // 重新设置剩余线程数量
        int newBalance = balance - maximumPoolSize;
        if (newBalance == 0) {
            defaultPoolExecutor.shutdown();
        } else {
            if (newBalance < defaultPoolExecutor.getCorePoolSize()) {
                defaultPoolExecutor.setCorePoolSize(newBalance);
            }
            defaultPoolExecutor.setMaximumPoolSize(newBalance);
        }
    }
	
    /**
     * 不存在，则创建
     * @param serviceUniqueName
     * @param corePoolSize
     * @param maximumPoolSize
     * @return
     * @throws Exception
     */
    public Executor getThreadExecutor(String serviceUniqueName,int corePoolSize, int maximumPoolSize) {
        if (!poolCache.isEmpty()) {
            ThreadPoolExecutor executor = poolCache.get(serviceUniqueName);
            if (executor != null) {
                return executor;
            }else{
            	try{
                	allocThreadPool(serviceUniqueName, corePoolSize, maximumPoolSize);
            	}catch(Exception e){
            		log.error("allocThreadPool exception",e);
            	}
            }
            executor = poolCache.get(serviceUniqueName);
            if(executor!=null){
            	return executor;
            }
        }else{
        	try{
            	allocThreadPool(serviceUniqueName, corePoolSize, maximumPoolSize);
        	}catch(Exception e){
        		log.error("allocThreadPool exception",e);
        	}
        	 ThreadPoolExecutor executor = poolCache.get(serviceUniqueName);
             if (executor != null) {
                 return executor;
             }
        }
        return defaultPoolExecutor;
    }
    
    public Executor getThreadExecutor(String serviceUniqueName) {
        if (!poolCache.isEmpty()) {
            ThreadPoolExecutor executor = poolCache.get(serviceUniqueName);
            if (executor != null) {
                return executor;
            }
        }
        return defaultPoolExecutor;
    }
	
    public void shutdown() {
        if (defaultPoolExecutor != null && !defaultPoolExecutor.isShutdown()) {
            defaultPoolExecutor.shutdown();
        }

        if (!poolCache.isEmpty()) {
            Iterator<ThreadPoolExecutor> ite = poolCache.values().iterator();
            while (ite.hasNext()) {
                ThreadPoolExecutor poolExecutor = ite.next();
                poolExecutor.shutdown();
            }
        }
    }
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder("当前线程池分配策略：");
        Iterator<Map.Entry<String, ThreadPoolExecutor>> ite = poolCache.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, ThreadPoolExecutor> entry = ite.next();
            String serviceUniqName = entry.getKey();
            ThreadPoolExecutor executor = entry.getValue();
            sb.append("服务[" + serviceUniqName + "]核心线程数量：" + executor.getCorePoolSize() + " 最大线程数量："
                    + executor.getMaximumPoolSize() + " 活动线程数量：" + executor.getActiveCount());
        }

        if (!defaultPoolExecutor.isShutdown()) {
            sb.append("服务默认使用的核心线程数量：" + defaultPoolExecutor.getCorePoolSize() + " 最大线程数量： "
                    + defaultPoolExecutor.getMaximumPoolSize() + " 活动线程数量：" + defaultPoolExecutor.getActiveCount());
        }

        return sb.toString();
    }
			

}
