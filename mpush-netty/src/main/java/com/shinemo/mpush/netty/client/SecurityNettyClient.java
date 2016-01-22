package com.shinemo.mpush.netty.client;



public  class SecurityNettyClient extends NettyClient {

	private byte[] clientKey;
	private byte[] iv;
	private String clientVersion;
	private String deviceId;
	private String osName;
	private String osVersion;
	
	private String userId;
	
	private String cipher; //快速重连的时候使用
	
	public SecurityNettyClient(String host, int port) {
		super(host, port);
	}

	public byte[] getClientKey() {
		return clientKey;
	}

	public void setClientKey(byte[] clientKey) {
		this.clientKey = clientKey;
	}

	public byte[] getIv() {
		return iv;
	}

	public void setIv(byte[] iv) {
		this.iv = iv;
	}

	public String getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getCipher() {
		return cipher;
	}

	public void setCipher(String cipher) {
		this.cipher = cipher;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}
