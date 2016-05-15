package com.mpush.tools.redis.consistenthash;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import redis.clients.util.Hashing;

public class ConsistentHash {

	private final Hashing hash;
	private final int numberOfReplicas;
	private final SortedMap<Long, Node> circle = new TreeMap<Long, Node>();

	public ConsistentHash(Hashing hash, int numberOfReplicas,
			Collection<Node> nodes) {
		super();
		this.hash = hash;
		this.numberOfReplicas = numberOfReplicas;
		for (Node node : nodes) {
			add(node);
		}
	}

	/**
	 * 增加真实机器节点
	 * 
	 * @param node
	 */
	public void add(Node node) {
		for (int i = 0; i < this.numberOfReplicas; i++) {
			circle.put(this.hash.hash(node.toString() + i), node);
		}
	}

	/**
	 * 删除真实机器节点
	 * 
	 * @param node
	 */
	public void remove(String node) {
		for (int i = 0; i < this.numberOfReplicas; i++) {
			circle.remove(this.hash.hash(node.toString() + i));
		}
	}

	/**
	 * 取得真实机器节点
	 * 
	 * @param key
	 * @return
	 */
	public Node get(String key) {
		if (circle.isEmpty()) {
			return null;
		}
		long hash = this.hash.hash(key);
		if (!circle.containsKey(hash)) {
			SortedMap<Long, Node> tailMap = circle.tailMap(hash);// 沿环的顺时针找到一个虚拟节点
			hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
		}
		return circle.get(hash); // 返回该虚拟节点对应的真实机器节点的信息
	}

	
}
