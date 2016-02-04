package com.shinemo.mpush.test.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import redis.clients.util.Hashing;
import redis.clients.util.MurmurHash;

import com.shinemo.mpush.tools.redis.consistenthash.ConsistentHash;
import com.shinemo.mpush.tools.redis.consistenthash.Node;

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
