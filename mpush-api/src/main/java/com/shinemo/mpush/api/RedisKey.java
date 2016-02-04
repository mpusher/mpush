package com.shinemo.mpush.api;

public class RedisKey {
	
	private static final String USER_PREFIX = "mp_u_";
	
	private static final String SESSION_PREFIX = "mp_s_";
	
	private static final String FAST_CONNECTION_DEVICE_PREFIX = "mp_f_c_d_";
	
	public static final String getUserKey(String userId){
		return USER_PREFIX+userId;
	}
	
	public static final String getSessionKey(String sessionId){
		return SESSION_PREFIX + sessionId;
	}
	
	//for fast connection test
	public static final String getDeviceIdKey(String deviceId){
		return FAST_CONNECTION_DEVICE_PREFIX+deviceId;
	}

}
