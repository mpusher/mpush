package com.mpush.tools.owner;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;


@Sources({"classpath:serverconfig.properties"})
public interface ServerConfig extends Config{

	@Key("zk_ip")
	@DefaultValue("zkIp")
	public String zkIp();
	
	@Key("zk_digest")
	public String zkDigest();
	
	@Key("hello")
	@DefaultValue("hello world")
	public String hello();
	
	@Key("max_hb_timeout_times")
	public int maxHbTimeoutTimes();
	
	@Key("test")
	@DefaultValue("10")
	public int test();
	
	public Integer testnotexist();
	
}
