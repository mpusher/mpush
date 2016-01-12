package com.shinemo.mpush.tools.spi;


public class TestServiceImpl2 implements TestService {

    public TestServiceImpl2() {
        System.out.println("2222222");
    }

    
	@Override
	public void sayHi(String name) {
		System.out.println("TestServiceImpl2 hi,"+name);
	}
	
}
