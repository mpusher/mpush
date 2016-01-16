package com.shinemo.mpush.zk;

import org.junit.Test;

import com.shinemo.mpush.tools.zk.ZkRegister;
import com.shinemo.mpush.tools.zk.curator.services.ZkRegisterManager;

public class ZkTest {
	
	@Test
	public void remove(){
		ZkRegister zkRegister = new ZkRegisterManager();
		
		zkRegister.init();
		
		zkRegister.remove("/");
		
	}

}
