package com.shinemo.mpush.monitor.quota.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.shinemo.mpush.monitor.quota.BaseQuota;
import com.shinemo.mpush.monitor.quota.MemoryQuota;

public class JVMMemory extends BaseQuota implements MemoryQuota {

	public static final JVMMemory instance = new JVMMemory();

	private MemoryMXBean memoryMXBean;

	private MemoryPoolMXBean permGenMxBean;
	private MemoryPoolMXBean oldGenMxBean;
	private MemoryPoolMXBean edenSpaceMxBean;
	private MemoryPoolMXBean pSSurvivorSpaceMxBean;

	private JVMMemory() {
		memoryMXBean = ManagementFactory.getMemoryMXBean();
		List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean item : list) {
			String name = item.getName();
			if (contain(name, permGenName)) {
				permGenMxBean = item;
			} else if (contain(name, oldGenName)) {
				oldGenMxBean = item;
			} else if (contain(name, edenSpaceName)) {
				edenSpaceMxBean = item;
			} else if (contain(name, psSurvivorName)) {
				pSSurvivorSpaceMxBean = item;
			}
		}
	}

	@Override
	public long heapMemoryCommitted() {
		return memoryMXBean.getHeapMemoryUsage().getCommitted();
	}

	@Override
	public long heapMemoryInit() {
		return memoryMXBean.getHeapMemoryUsage().getInit();
	}

	@Override
	public long heapMemoryMax() {
		return memoryMXBean.getHeapMemoryUsage().getMax();
	}

	@Override
	public long heapMemoryUsed() {
		return memoryMXBean.getHeapMemoryUsage().getUsed();
	}

	@Override
	public long nonHeapMemoryCommitted() {
		return memoryMXBean.getNonHeapMemoryUsage().getCommitted();
	}

	@Override
	public long nonHeapMemoryInit() {
		return memoryMXBean.getNonHeapMemoryUsage().getInit();
	}

	@Override
	public long nonHeapMemoryMax() {
		return memoryMXBean.getNonHeapMemoryUsage().getMax();
	}

	@Override
	public long nonHeapMemoryUsed() {
		return memoryMXBean.getNonHeapMemoryUsage().getUsed();
	}

	@Override
	public long permGenCommitted() {
		if (permGenMxBean == null) {
			return 0;
		}
		return permGenMxBean.getUsage().getCommitted();
	}

	@Override
	public long permGenInit() {
		if (permGenMxBean == null) {
			return 0;
		}
		return permGenMxBean.getUsage().getInit();
	}

	@Override
	public long permGenMax() {
		if (permGenMxBean == null) {
			return 0;
		}
		return permGenMxBean.getUsage().getMax();
	}

	@Override
	public long permGenUsed() {
		if (permGenMxBean == null) {
			return 0;
		}
		return permGenMxBean.getUsage().getUsed();
	}

	@Override
	public long oldGenCommitted() {
		if (oldGenMxBean == null) {
			return 0;
		}
		return oldGenMxBean.getUsage().getCommitted();
	}

	@Override
	public long oldGenInit() {
		if (oldGenMxBean == null) {
			return 0;
		}
		return oldGenMxBean.getUsage().getInit();
	}

	@Override
	public long oldGenMax() {
		if (oldGenMxBean == null) {
			return 0;
		}
		return oldGenMxBean.getUsage().getMax();
	}

	@Override
	public long oldGenUsed() {
		if (oldGenMxBean == null) {
			return 0;
		}
		return oldGenMxBean.getUsage().getUsed();
	}

	@Override
	public long edenSpaceCommitted() {
		if (null == edenSpaceMxBean) {
			return 0;
		}
		return edenSpaceMxBean.getUsage().getCommitted();
	}

	@Override
	public long edenSpaceInit() {
		if (null == edenSpaceMxBean) {
			return 0;
		}
		return edenSpaceMxBean.getUsage().getInit();
	}

	@Override
	public long edenSpaceMax() {
		if (null == edenSpaceMxBean) {
			return 0;
		}
		return edenSpaceMxBean.getUsage().getMax();
	}

	@Override
	public long edenSpaceUsed() {
		if (null == edenSpaceMxBean) {
			return 0;
		}
		return edenSpaceMxBean.getUsage().getUsed();
	}

	@Override
	public long survivorCommitted() {
		if (null == pSSurvivorSpaceMxBean) {
			return 0;
		}
		return pSSurvivorSpaceMxBean.getUsage().getCommitted();
	}

	@Override
	public long survivorInit() {
		if (null == pSSurvivorSpaceMxBean) {
			return 0;
		}
		return pSSurvivorSpaceMxBean.getUsage().getInit();
	}

	@Override
	public long survivorMax() {
		if (null == pSSurvivorSpaceMxBean) {
			return 0;
		}
		return pSSurvivorSpaceMxBean.getUsage().getMax();
	}

	@Override
	public long survivorUsed() {
		if (null == pSSurvivorSpaceMxBean) {
			return 0;
		}
		return pSSurvivorSpaceMxBean.getUsage().getUsed();
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String,Object> map = Maps.newHashMap();
		map.put("heapMemoryCommitted", heapMemoryCommitted());
		map.put("heapMemoryInit", heapMemoryInit());
		map.put("heapMemoryMax", heapMemoryMax());
		map.put("heapMemoryUsed", heapMemoryUsed());
		map.put("nonHeapMemoryCommitted", nonHeapMemoryCommitted());
		map.put("nonHeapMemoryInit", nonHeapMemoryInit());
		map.put("nonHeapMemoryMax", nonHeapMemoryMax());
		map.put("nonHeapMemoryUsed", nonHeapMemoryUsed());
		map.put("permGenCommitted", permGenCommitted());
		map.put("permGenInit", permGenInit());
		map.put("permGenMax", permGenMax());
		map.put("permGenUsed", permGenUsed());
		map.put("oldGenCommitted", oldGenCommitted());
		map.put("oldGenInit", oldGenInit());
		map.put("oldGenMax", oldGenMax());
		map.put("oldGenUsed", oldGenUsed());
		map.put("edenSpaceCommitted", edenSpaceCommitted());
		map.put("edenSpaceInit", edenSpaceInit());
		map.put("edenSpaceMax", edenSpaceMax());
		map.put("edenSpaceUsed", edenSpaceUsed());
		map.put("survivorCommitted", survivorCommitted());
		map.put("survivorInit", survivorInit());
		map.put("survivorMax", survivorMax());
		map.put("survivorUsed", survivorUsed());
		return map;
	}

}
