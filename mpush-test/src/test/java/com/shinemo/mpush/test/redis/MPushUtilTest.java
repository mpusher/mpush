package com.shinemo.mpush.test.redis;

import org.junit.Test;

import com.shinemo.mpush.tools.MPushUtil;

public class MPushUtilTest {
	
	@Test
	public void getIp() throws Exception{
		System.out.println(MPushUtil.getExtranetAddress());
	}

}
