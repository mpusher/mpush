package com.shinemo.mpush.log;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerManage {
	
	private static final Logger connectionLog = LoggerFactory.getLogger("connectionLog");
	private static final Logger pushLog = LoggerFactory.getLogger("pushLog");
	private static final Logger heartBeatLog = LoggerFactory.getLogger("heartBeatLog");
	private static final Logger redisLog = LoggerFactory.getLogger("redisLog");
	private static final Logger zkLog = LoggerFactory.getLogger("zkLog");
	
	private static final Logger defaultLog = LoggerFactory.getLogger(LoggerManage.class);
	
	private static final Map<LogType, Logger> map = new HashMap<>();
	
	static{
		map.put(LogType.CONNECTION, connectionLog);
		map.put(LogType.PUSH, pushLog);
		map.put(LogType.HEARTBEAT, heartBeatLog);
		map.put(LogType.REDIS, redisLog);
		map.put(LogType.ZK, zkLog);
	}
	
	
	public static void log(LogType type,String format,Object... arguments){
		log(type, null, format, arguments);
	}
	
	public static Logger getLog(LogType type){
		Logger log = map.get(type);
		if(log == null){
			log = defaultLog;
		}
		return log;
	}
	
	public static void log(LogType type,Throwable ex,String format,Object... arguments){
		String ret = String.format(format, arguments);
		Logger log = map.get(type);
		if(ex!=null){
			if(log!=null){
				log.info(ret,ex);
			}else{
				defaultLog.info(ret,ex);
			}
		}else{
			if(log!=null){
				log.info(ret);
			}else{
				defaultLog.info(ret);
			}
		}
	}
	
	/**
	 * security的log 为 connectionLog的log
	 * @param security
	 * @param format
	 * @param arguments
	 */
	public static void log(boolean security,String format,Object... arguments){
		String ret = String.format(format, arguments);
		if(security){
			connectionLog.info(ret);
		}else{
			pushLog.info(ret);
		}
	}
	
	public static void main(String[] args) {
		String format = "client connect channel=%s";
		System.out.println(String.format(format, "hi"));
	}
	
}
