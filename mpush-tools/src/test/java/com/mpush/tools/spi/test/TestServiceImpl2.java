package com.mpush.tools.spi.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestServiceImpl2 implements TestService {

	private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);
	
    public TestServiceImpl2() {
    	log.warn("init");
    }
    
	@Override
	public String sayHi(String name) {
		return "TestServiceImpl2 hi,"+name;
	}
	
}
