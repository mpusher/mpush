package com.mpush.api.event;

import com.mpush.api.connection.Connection;

/**
 * 绑定用户的时候才会触发该事件
 */
public final class UserOnlineEvent implements Event {
    
	private final Connection connection;
    private final String userId;
    
	public UserOnlineEvent(Connection connection, String userId) {
		this.connection = connection;
		this.userId = userId;
	}
	
	public Connection getConnection() {
		return connection;
	}
	public String getUserId() {
		return userId;
	}
    

}
