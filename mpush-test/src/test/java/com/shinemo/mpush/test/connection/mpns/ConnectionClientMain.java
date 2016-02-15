package com.shinemo.mpush.test.connection.mpns;

import java.util.List;

import com.google.common.collect.Lists;
import com.shinemo.mpush.common.AbstractClient;
import com.shinemo.mpush.cs.ConnectionServerApplication;

public class ConnectionClientMain extends AbstractClient {


	private static final List<ConnectionServerApplication> applicationLists = Lists.newArrayList(new ConnectionServerApplication(20882,"","111.1.57.148"));
	
	public List<ConnectionServerApplication> getApplicationList(){
		return Lists.newArrayList(applicationLists);
	}
}
