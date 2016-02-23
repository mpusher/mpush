package com.shinemo.mpush.api.event;

import com.shinemo.mpush.api.connection.Connection;

/**
 * 链接超时，用户解绑的时候，才会触发该事件 
 *
 */
public final class UserOfflineEvent implements Event {
	
	private final Connection connection;
    private final String userId;
    
	public UserOfflineEvent(Connection connection, String userId) {
		this.connection = connection;
		this.userId = userId;
	}
	
	public UserOfflineEvent(Connection connection) {
		this.connection = connection;
		this.userId = null;
	}
	
	public Connection getConnection() {
		return connection;
	}
	public String getUserId() {
		return userId;
	}
}
