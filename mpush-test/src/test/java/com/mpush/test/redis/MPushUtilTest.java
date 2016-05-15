package com.mpush.test.redis;

import com.mpush.tools.MPushUtil;
import org.junit.Test;

public class MPushUtilTest {
	
	@Test
	public void getIp() throws Exception{
		System.out.println(MPushUtil.getExtranetAddress());
	}

}
