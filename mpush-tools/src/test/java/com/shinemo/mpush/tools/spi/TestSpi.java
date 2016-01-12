package com.shinemo.mpush.tools.spi;

public class TestSpi {

	public static void main(String[] args) {
		
		
		TestService testService = ServiceContainer.getInstance(TestService.class,"test2");
		
		testService.sayHi("huang");
		
	}
	
}

