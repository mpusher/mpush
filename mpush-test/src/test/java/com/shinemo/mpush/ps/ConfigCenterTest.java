package com.shinemo.mpush.ps;

import org.junit.Test;

import com.shinemo.mpush.tools.config.ConfigCenter;

public class ConfigCenterTest {
	
	@Test
	public void test(){
		
		System.out.println(ConfigCenter.holder.zkIp());
		
	}

}
