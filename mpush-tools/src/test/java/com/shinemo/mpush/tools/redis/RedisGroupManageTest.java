package com.shinemo.mpush.tools.redis;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.redis.manage.RedisGroupManage;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

public class RedisGroupManageTest {
	
	ServerApp app = new ServerApp(InetAddressUtil.getInetAddress(),"3000");
	ServerManage manage = new ServerManage(app);
	List<RedisGroup> groupList = null;
	
	@Before
	public void init(){
		manage.start();
		groupList = RedisGroupManage.instance.getGroupList();
	}
	
	@Test
	public void testGetRedisGroup(){
		for(RedisGroup group:groupList){
			for(RedisNode node:group.getRedisNodeList()){
				System.out.println(group+ToStringBuilder.reflectionToString(node, ToStringStyle.MULTI_LINE_STYLE));
			}
			
		}
	}
	
	

}
