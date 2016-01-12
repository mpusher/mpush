package com.shinemo.mpush.tools.spi;


import com.shinemo.mpush.tools.spi.test.TestService;

public class SpiTest {
	
	public static void main(String[] args) {
		TestService testService = ServiceContainer.getInstance(TestService.class);
		System.out.println(testService.sayHi(" huang"));
	}

}
