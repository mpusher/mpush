package com.mpush.tools.owner;

import com.mpush.tools.config.ConfigCenter;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

public class OwnerTest {
	
	@Test
	public void test1(){
		
		ServerConfig cfg = ConfigFactory.create(ServerConfig.class);
		
		System.out.println(cfg.zkDigest());
		
		System.out.println(cfg.zkIp());
		
		System.out.println(cfg.hello());
		
		System.out.println(cfg.maxHbTimeoutTimes());
		
		System.out.println(cfg.test());
		
		Integer tset = cfg.testnotexist();
		if(tset == null){
			System.out.println("not exist");
		}else{
			System.out.println(tset);
		}
	}
	
	@Test
	public void test2(){
		
		System.out.println(ConfigCenter.I.zkIp());
		
		System.out.println("aesKeyLength:"+ConfigCenter.I.aesKeyLength());
		
		System.out.println("compressLimit:"+ConfigCenter.I.compressLimit());
		
		System.out.println("connectionServerPort:"+ConfigCenter.I.connectionServerPort());
		
		System.out.println("gatewayServerPort:"+ConfigCenter.I.gatewayServerPort());
		
		System.out.println("maxHBTimeoutTimes:"+ConfigCenter.I.maxHBTimeoutTimes());
		
		System.out.println(ConfigCenter.I.maxHeartbeat());
		
		System.out.println(ConfigCenter.I.maxPacketSize());
		
		System.out.println(ConfigCenter.I.minHeartbeat());
		
		System.out.println(ConfigCenter.I.privateKey());
		
		System.out.println(ConfigCenter.I.publicKey());
		
		System.out.println(ConfigCenter.I.rsaKeyLength());
		
		System.out.println(ConfigCenter.I.redisIp());
		
		System.out.println(ConfigCenter.I.sessionExpiredTime());
		
		System.out.println(ConfigCenter.I.zkDigest());
		

		
		System.out.println(ConfigCenter.I.zkNamespace());
	}

}
