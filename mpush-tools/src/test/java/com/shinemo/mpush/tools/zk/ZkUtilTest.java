package com.shinemo.mpush.tools.zk;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

public class ZkUtilTest {
	
	private ZkConfig zkConfig = new ZkConfig("127.0.0.1:2181", "huang6", 5, 3000, 6000, 3000, 3000, null);
	
	@Test
	public void test(){
		ZkUtil zkUtil = new ZkUtil(zkConfig);
		
		zkUtil.init();
		
		String dubbo = zkUtil.get("/dubbo");
		System.out.println(dubbo);
		
		List<String> child = zkUtil.getChildrenKeys("/dubbo");
		System.out.println(ToStringBuilder.reflectionToString(child, ToStringStyle.JSON_STYLE));
		
		zkUtil.putEphemeral("/huang", "hi");
		zkUtil.putEphemeralSequential("/huang2");
		
		String huang = zkUtil.get("/huang");
		System.out.println(huang);
		
		String huang2 = zkUtil.get("/huang2");
		System.out.println(huang2);
		
	}

}
