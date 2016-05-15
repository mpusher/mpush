package com.mpush.test.configcenter;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mpush.tools.Jsons;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.dns.DnsMapping;

public class ConfigCenterTest {
	
	@Test
	public void test(){
		
		System.out.println(ConfigCenter.I.forceWriteRedisGroupInfo());
		
	}
	
	@Test
	public void testDnsMapping(){
		Map<String, List<DnsMapping>> ret = ConfigCenter.I.dnsMapping();
		
		System.out.println(Jsons.toJson(ret));
		
	}

}
