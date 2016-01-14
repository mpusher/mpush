package com.shinemo.mpush.core.test;

import org.aeonbits.owner.ConfigFactory;

import com.shinemo.mpush.tools.config.ConfigCenter;

public class ConfigCenterTest {
	
	public static void main(String[] args) {
		
		ConfigCenter holder = ConfigFactory.create(ConfigCenter.class);
		
		System.out.println(holder.zkIp());
		
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
		
		System.out.println(ConfigCenter.holder.zkIp());
		
		System.out.println(ConfigCenter.holder.zkNamespace());
    }

}
