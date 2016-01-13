package com.shinemo.mpush.tools.spi.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServiceImpl implements TestService {

	private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);
	
    public TestServiceImpl() {
    	log.warn("init");
    }

	@Override
	public String sayHi(String name) {
		return "TestServiceImpl1 hi,"+name;
	}

}
