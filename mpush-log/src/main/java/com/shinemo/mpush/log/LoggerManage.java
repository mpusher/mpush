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
	private static final Logger httpLog = LoggerFactory.getLogger("httpLog");
	
	private static final Logger defaultLog = LoggerFactory.getLogger(LoggerManage.class);
	
	private static final Map<LogType, Logger> map = new HashMap<>();
	
	static{
		map.put(LogType.CONNECTION, connectionLog);
		map.put(LogType.PUSH, pushLog);
		map.put(LogType.HEARTBEAT, heartBeatLog);
		map.put(LogType.REDIS, redisLog);
		map.put(LogType.ZK, zkLog);
		map.put(LogType.HTTP, httpLog);
	}
	
	
	public static void log(LogType type,String format,Object... arguments){
		info(type, format, arguments);
	}
	
	public static void warn(LogType type,String format,Object... arguments){
		log(type, LogLevel.WARN, null, format, arguments);
	}
	
	public static void info(LogType type,String format,Object... arguments){
		log(type, LogLevel.INFO, null, format, arguments);
	}
	
	public static void error(LogType type,String format,Object... arguments){
		log(type, LogLevel.ERROR, null, format, arguments);
	}
	
	public static void debug(LogType type,String format,Object... arguments){
		log(type, LogLevel.DEBUG, null, format, arguments);
	}
	
	public static Logger getLog(LogType type){
		Logger log = map.get(type);
		if(log == null){
			log = defaultLog;
		}
		return log;
	}
	
	public static void execption(LogType type,Throwable ex,String format,Object... arguments){
		log(type, LogLevel.ERROR, ex, format, arguments);
	}
	
	/**
	 * 默认 level 为warn
	 * @param type
	 * @param level
	 * @param ex
	 * @param format
	 * @param arguments
	 */
	public static void log(LogType type,LogLevel level,Throwable ex,String format,Object... arguments){
		if(level == null){
			level = LogLevel.WARN;
		}
		Logger log = map.get(type);
		if(ex!=null){
			if(log!=null){
				if(level.equals(LogLevel.WARN)){
					log.warn(format,arguments,ex);
				}else if(level.equals(LogLevel.INFO)){
					log.info(format,arguments,ex);
				}else if(level.equals(LogLevel.DEBUG)){
					log.debug(format,arguments,ex);
				}else if(level.equals(LogLevel.ERROR)){
					log.error(format,arguments,ex);
				}
			}else{
				defaultLog.warn(format,arguments,ex);
			}
		}else{
			if(log!=null){
				if(level.equals(LogLevel.WARN)){
					log.warn(format,arguments);
				}else if(level.equals(LogLevel.INFO)){
					log.info(format,arguments);
				}else if(level.equals(LogLevel.DEBUG)){
					log.debug(format,arguments);
				}else if(level.equals(LogLevel.ERROR)){
					log.error(format,arguments);
				}
			}else{
				defaultLog.warn(format,arguments);
			}
		}
	}
	

	/**
	 * security的log 为 connectionLog的log
	 * @param security
	 * @param level
	 * @param format
	 * @param arguments
	 */
	public static void log(boolean security,LogLevel level,String format,Object... arguments){
		if(security){
			log(LogType.CONNECTION, level, null, format, arguments);
		}else{
			log(LogType.PUSH, level, null, format, arguments);
		}
	}	
	public static void main(String[] args) {
		String format = "client connect channel=%s";
		System.out.println(String.format(format, "hi"));
	}
	
}
