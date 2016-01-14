package com.shinemo.mpush.tools.config;


import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Config.Sources;

@Sources({"classpath:config.properties"})
public interface ConfigCenter extends Config{
	
	public static ConfigCenter holder = ConfigFactory.create(ConfigCenter.class);
	
	@Key("max_packet_size")
	@DefaultValue("10240")
	public int maxPacketSize();
	
	@Key("compress_limit")
	@DefaultValue("10240")
	public int compressLimit();
	
	@Key("min_heartbeat")
	@DefaultValue("10000") //10s
	public int minHeartbeat();
	
	@Key("max_heartbeat")
	@DefaultValue("1800000") //30min
	public int maxHeartbeat();
	
	@Key("max_hb_timeout_times")
	@DefaultValue("2")
	public int maxHBTimeoutTimes();
	
	@Key("session_expired_time")
	@DefaultValue("86400") //unit second
	public int sessionExpiredTime();
	
	@Key("ras_key_length")
	@DefaultValue("1024")
	public int rasKeyLength();
	
	@Key("aes_key_length")
	@DefaultValue("16")
	public int aesKeyLength();
	
	@Key("connection_server_port")
	@DefaultValue("3000")
	public int connectionServerPort();
	
	@Key("gateway_server_port")
	@DefaultValue("4000")
	public int gatewayServerPort();
	
	@Key("private_key")
	@DefaultValue("MIIBNgIBADANBgkqhkiG9w0BAQEFAASCASAwggEcAgEAAoGBAKCE8JYKhsbydMPbiO7BJVq1pbuJWJHFxOR7L8Hv3ZVkSG4eNC8DdwAmDHYu/wadfw0ihKFm2gKDcLHp5yz5UQ8PZ8FyDYvgkrvGV0ak4nc40QDJWws621dm01e/INlGKOIStAAsxOityCLv0zm5Vf3+My/YaBvZcB5mGUsPbx8fAgEAAoGAAy0+WanRqwRHXUzt89OsupPXuNNqBlCEqgTqGAt4Nimq6Ur9u2R1KXKXUotxjp71Ubw6JbuUWvJg+5Rmd9RjT0HOUEQF3rvzEepKtaraPhV5ejEIrB+nJWNfGye4yzLdfEXJBGUQzrG+wNe13izfRNXI4dN/6Q5npzqaqv0E1CkCAQACAQACAQACAQACAQA=")
	public String privateKey();
	
	@Key("public_key")
	@DefaultValue("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB")
	public String publicKey();
	
	@Key("redis_ip")
	@DefaultValue("127.0.0.1:6379:ShineMoIpo")
	public String redisIp();
	
	@Key("zk_ip")
	@DefaultValue("127.0.0.1:2181")
	public String zkIp();
	
	@Key("zk_namespace")
	@DefaultValue("mpush")
	public String zkNamespace();
	
	@Key("zk_digest")
	@DefaultValue("shinemoIpo")
	public String zkDigest();
	
}
