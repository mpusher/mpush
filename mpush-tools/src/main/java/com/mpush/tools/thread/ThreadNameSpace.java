package com.mpush.tools.thread;

public class ThreadNameSpace {

    /**
     * netty boss 线程
     */
    public static final String NETTY_BOSS = "mp-boss";

    /**
     * netty worker 线程
     */
    public static final String NETTY_WORKER = "mp-worker";
    
    public static final String NETTY_HTTP = "mp-http";
    
    public static final String EVENT_BUS = "mp-event-bus";
    
    public static final String REDIS = "mp-redis";
    
    public static final String ZK = "mp-zk";
    
    public static final String BIZ = "mp-biz";

    /**
     * connection 定期检测线程
     */
    public static final String NETTY_TIMER = "mp-timer";

    public static final String getUniqueName(String serviceName) {
        return "mp-sn-" + serviceName;
    }
    
    public static final String THREAD_NAME_PREFIX = "mp-t-";

    public static final String getServerName(String serverName){
    	if(serverName.equals("ConnectionServer")){
        	return "mp-start-cs";
    	}else if(serverName.equals("GatewayServer")){
        	return "mp-start-gs";
    	}else{
        	return "mp-start-unknow";
    	}
    }
    
}
