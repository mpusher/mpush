package com.mpush.core.router;

/**
 * Created by ohun on 2016/1/4.
 *
 * @author ohun@live.cn
 */
public final class KickRemoteMsg {
	public String userId;
	public String deviceId;
	public String targetServer;

	@Override
	public String toString() {
		return "KickRemoteMsg{" + "userId='" + userId + '\'' + ", deviceId='" + deviceId + '\'' + ", targetServer='" + targetServer + '\'' + '}';
	}
}
