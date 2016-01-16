package com.shinemo.mpush;

import org.junit.Test;

import com.shinemo.mpush.cs.ConnectionServerApplication;
import com.shinemo.mpush.tools.Jsons;

public class ConnectionServerApplicationTest {
	
	@Test
	public void testJson(){
		
		ConnectionServerApplication application = new ConnectionServerApplication();
		
		String str = Jsons.toJson(application);
		
		System.out.println(str);
		
	}

}
