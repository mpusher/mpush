package com.shinemo.mpush.tools.spi;

public class TestServiceImpl implements TestService {

    public TestServiceImpl() {
        System.out.println("1111111");
    }

	@Override
	public void sayHi(String name) {
		System.out.println("TestServiceImpl hi,"+name);
	}

}
