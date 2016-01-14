package com.shinemo.mpush.tools.owner;

import org.aeonbits.owner.ConfigFactory;

public class OwnerTest {
	
	public static void main(String[] args) {
		
		ServerConfig cfg = ConfigFactory.create(ServerConfig.class);
		
		System.out.println(cfg.zkDigest());
		
		System.out.println(cfg.zkIp());
		
		System.out.println(cfg.hello());
		
		
	}

}
