package com.mpush.tools.config;

import java.lang.reflect.Method;

import com.mpush.tools.redis.RedisGroup;
import org.aeonbits.owner.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpush.tools.redis.RedisNode;

public class RedisGroupConverter implements Converter<RedisGroup>{

	private static final Logger log = LoggerFactory.getLogger(RedisGroupConverter.class);
	
	@Override
	public RedisGroup convert(Method method, String input) {
		
		log.warn("method:"+method.getName()+","+input);
		

        RedisGroup group = new RedisGroup();
		
		String[] chunks = input.split(",");
        for (String chunk : chunks) {
            String[] entry = chunk.split(":");
            String ip = entry[0].trim();
            String port = entry[1].trim();
            // 如果配置了redis密码（redis_group = 111.1.57.148:6379:ShineMoIpo）才设置密码
            // 否则密码为空，JedisPool可以兼容两种情况
            String password = null; 
            if(entry.length >=3){
            	password = entry[2].trim();
            }
            RedisNode node = new RedisNode(ip, Integer.parseInt(port), password);
            group.addRedisNode(node);
        }
		return group;
	}
	
	

}
