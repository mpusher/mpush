package com.shinemo.mpush.tools.owner;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import com.shinemo.mpush.tools.config.ConfigCenter;

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
		
		System.out.println(ConfigCenter.holder.zkIp());
		
		System.out.println("aesKeyLength:"+ConfigCenter.holder.aesKeyLength());
		
		System.out.println("compressLimit:"+ConfigCenter.holder.compressLimit());
		
		System.out.println("connectionServerPort:"+ConfigCenter.holder.connectionServerPort());
		
		System.out.println("gatewayServerPort:"+ConfigCenter.holder.gatewayServerPort());
		
		System.out.println("maxHBTimeoutTimes:"+ConfigCenter.holder.maxHBTimeoutTimes());
		
		System.out.println(ConfigCenter.holder.maxHeartbeat());
		
		System.out.println(ConfigCenter.holder.maxPacketSize());
		
		System.out.println(ConfigCenter.holder.minHeartbeat());
		
		System.out.println(ConfigCenter.holder.privateKey());
		
		System.out.println(ConfigCenter.holder.publicKey());
		
		System.out.println(ConfigCenter.holder.rasKeyLength());
		
		System.out.println(ConfigCenter.holder.redisIp());
		
		System.out.println(ConfigCenter.holder.sessionExpiredTime());
		
		System.out.println(ConfigCenter.holder.zkDigest());
		

		
		System.out.println(ConfigCenter.holder.zkNamespace());
	}

}
