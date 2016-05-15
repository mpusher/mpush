package com.mpush.test.util;

import java.net.URI;
import java.net.URISyntaxException;

import com.mpush.tools.MPushUtil;
import org.junit.Test;

public class TelnetTest {

	@Test
	public void test(){
		boolean ret = MPushUtil.telnet("120.27.196.68", 82);
		System.out.println(ret);
	}
	
	@Test
	public void test2(){
		boolean ret = MPushUtil.telnet("120.27.196.68", 80);
		System.out.println(ret);
	}
	
	@Test
	public void uriTest() throws URISyntaxException{
		String url = "http://127.0.0.1";
		URI uri = new URI(url);
		System.out.println(uri.getPort());
	}
	
	
}
