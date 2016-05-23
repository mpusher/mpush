/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.test.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mpush.cache.redis.hash.ConsistentHash;
import com.mpush.cache.redis.hash.Node;
import org.junit.Test;

import redis.clients.util.Hashing;
import redis.clients.util.MurmurHash;

public class ConsistentHashTest {
	
	private static final String IP_PREFIX = "192.168.1.";// 机器节点IP前缀
	
	@Test
	public void test(){
		Map<String, Integer> map = new HashMap<String, Integer>();// 每台真实机器节点上保存的记录条数
		List<Node> nodes = new ArrayList<Node>();// 真实机器节点
		// 10台真实机器节点集群
		for (int i = 1; i <= 10; i++) {
			map.put(IP_PREFIX + i, 0);// 每台真实机器节点上保存的记录条数初始为0
			Node node = new Node(IP_PREFIX + i, "node" + i);
			nodes.add(node);
		}
		Hashing hashFunction = new MurmurHash(); // hash函数实例
		ConsistentHash consistentHash = new ConsistentHash(hashFunction, 100, Collections.unmodifiableCollection(nodes));// 每台真实机器引入100个虚拟节点
		// 将5000条记录尽可能均匀的存储到10台机器节点
		for (int i = 0; i < 5000; i++) {
			// 产生随机一个字符串当做一条记录，可以是其它更复杂的业务对象,比如随机字符串相当于
			String data = UUID.randomUUID().toString() + i;
			// 通过记录找到真实机器节点
			Node node = consistentHash.get(data);
			// 再这里可以能过其它工具将记录存储真实机器节点上，比如MemoryCache等
			// 每台真实机器节点上保存的记录条数加1
			map.put(node.getIp(), map.get(node.getIp()) + 1);
		}
		// 打印每台真实机器节点保存的记录条数
		for (int i = 1; i <= 10; i++) {
			System.out.println(IP_PREFIX + i + "节点记录条数："
					+ map.get("192.168.1." + i));
		}
		
	}

}
