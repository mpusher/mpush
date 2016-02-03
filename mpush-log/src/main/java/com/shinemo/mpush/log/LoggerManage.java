package com.shinemo.mpush.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerManage {
	
	private static final Logger connectionLog = LoggerFactory.getLogger("connectionLog");
	private static final Logger pushLog = LoggerFactory.getLogger("pushLog");
	
	public static void log(LogType type,String format,Object... arguments){
		String ret = String.format(format, arguments);
		if(type.equals(LogType.CONNECTION)){
			connectionLog.info(ret);
		}else if(type.equals(LogType.PUSH)){
			pushLog.info(ret);
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
