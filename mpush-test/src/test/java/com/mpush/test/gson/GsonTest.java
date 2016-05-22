package com.mpush.test.gson;

import java.util.Map;

import com.mpush.tools.Jsons;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.mpush.api.push.PushContent;
import com.mpush.api.push.PushContent.PushType;

public class GsonTest {
	
	@Test
	public void test(){
		Map<String,String> map = Maps.newHashMap();
		map.put("key1", 1121+"");
		map.put("key2", "value2");
		
		PushContent content = PushContent.build(PushType.MESSAGE, Jsons.toJson(map));
		
		
		System.out.println(Jsons.toJson(content));
		
	}
	
	@Test
	public void test2(){
		ValueMap map = new ValueMap("1122", "value2");
		
		PushContent content = PushContent.build(PushType.MESSAGE,Jsons.toJson(map));
		
		
		System.out.println(Jsons.toJson(content));
		
		PushContent newContetn = Jsons.fromJson(Jsons.toJson(content), PushContent.class);
		
		System.out.println(newContetn.getContent());
		
		
	}
	
	private static class ValueMap{
		
		private String key1;
		private String key2;
		
		public ValueMap(String key1, String key2) {
			this.key1 = key1;
			this.key2 = key2;
		}

		public String getKey1() {
			return key1;
		}

		public String getKey2() {
			return key2;
		}
		
		
	}

}
