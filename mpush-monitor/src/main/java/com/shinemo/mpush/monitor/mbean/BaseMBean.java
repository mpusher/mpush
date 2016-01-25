package com.shinemo.mpush.monitor.mbean;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public abstract class BaseMBean {

	protected final static List<String> fullGcName = Lists.newArrayList("ConcurrentMarkSweep", "MarkSweepCompact", "PS MarkSweep", "G1 Old Generation",
			"Garbage collection optimized for short pausetimes Old Collector", "Garbage collection optimized for throughput Old Collector",
			"Garbage collection optimized for deterministic pausetimes Old Collector");

	protected final static List<String> youngGcName = Lists.newArrayList("ParNew", "Copy", "PS Scavenge", "G1 Young Generation", "Garbage collection optimized for short pausetimes Young Collector",
			"Garbage collection optimized for throughput Young Collector", "Garbage collection optimized for deterministic pausetimes Young Collector");

	protected final static List<String> permGenName = Lists.newArrayList("CMS Perm Gen", "Perm Gen", "PS Perm Gen", "G1 Perm Gen");

	protected final static List<String> oldGenName = Lists.newArrayList("CMS Old Gen", "Tenured Gen", "PS Old Gen", "G1 Old Gen");

	protected final static List<String> edenSpaceName = Lists.newArrayList("Par Eden Space", "Eden Space", "PS Eden Space", "G1 Eden");

	protected final static List<String> psSurvivorName = Lists.newArrayList("Par Survivor Space", "Survivor Space", "PS Survivor Space", "G1 Survivor");

	public static boolean contain(String name, List<String> list) {
		for (String str : list) {
			if (name.equals(str)) {
				return true;
			}
		}
		return false;
	}
	
	public abstract Map<String,Object> toMap();
}
