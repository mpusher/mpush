package com.shinemo.mpush.test.redis;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.redis.RedisNode;
import com.shinemo.mpush.tools.redis.RedisRegister;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import com.shinemo.mpush.tools.redis.pubsub.Subscriber;
import com.shinemo.mpush.tools.spi.ServiceContainer;

public class PubSubTest {
	
    private RedisRegister redisRegister = ServiceContainer.getInstance(RedisRegister.class);
    
    @Before
    public void init(){
    	RedisNode node = new RedisNode("127.0.0.1", 6379, "shinemoIpo");
    	RedisGroup group = new RedisGroup();
    	group.addRedisNode(node);
    	List<RedisGroup> listGroup = Lists.newArrayList(group);
    	redisRegister.init(listGroup);
    }
	
	@Test
	public void pubSubTest(){
		
		RedisManage.subscribe(Subscriber.holder, "/hello/123");
		
		RedisManage.subscribe(Subscriber.holder, "/hello/124");
		
		RedisManage.publish("/hello/123", "123");
		RedisManage.publish("/hello/124", "124");
	}
	
}
