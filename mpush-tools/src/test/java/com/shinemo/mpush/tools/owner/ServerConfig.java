package com.shinemo.mpush.tools.owner;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;


@Sources({"classpath:serverconfig.properties"})
public interface ServerConfig extends Config{

	@Key("zk_ip")
	public String zkIp();
	
	@Key("zk_digest")
	public String zkDigest();
	
	@Key("hello")
	@DefaultValue("hello world")
	public String hello();
	
	
}
