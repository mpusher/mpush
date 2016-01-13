package com.shinemo.mpush.tools.spi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ServiceContainer {

	private static final Logger log = LoggerFactory.getLogger(ServiceContainer.class);
	private static final String PREFIX = "META-INF/mpush/services/";

	// class -> ( beanId -> beanClass )
	private static final ConcurrentMap<Class<?>, Map<String, Class<?>>> clazzCacheMap = Maps.newConcurrentMap();

	// class -> ( beanId -> beanInstance)
	private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Object>> objectsCachedMap = Maps.newConcurrentMap();
	
	public static <T> T getInstance(Class<T> clazz) {

		if (clazz == null)
			throw new IllegalArgumentException("type == null");
		if (!clazz.isInterface()) {
			throw new IllegalArgumentException(" type(" + clazz + ") is not interface!");
		}
		if (!clazz.isAnnotationPresent(SPI.class)) {
			throw new IllegalArgumentException("type(" + clazz + ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
		}

		SPI spi = clazz.getAnnotation(SPI.class);
		String instanceName = spi.value();

		if (StringUtils.isBlank(instanceName)) {
			instanceName = toLowerCaseFirstOne(clazz.getName());
		}

		return getInstance(clazz, instanceName);

	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getInstances(Class<T> clazz){
		ConcurrentMap<String, Object> objMap = objectsCachedMap.get(clazz);
		if (objMap == null) {
			synchronized (clazz) {
				objMap = objectsCachedMap.get(clazz);
				if(objMap == null){
					objMap = Maps.newConcurrentMap();
					objectsCachedMap.put(clazz, objMap);
				}
			}
		}
		objMap = objectsCachedMap.get(clazz);
		if(!objMap.isEmpty()){
			return Lists.newArrayList((Collection<T>)objMap.values());
		}
		
		initClazzInstances(clazz);
		
		objMap = objectsCachedMap.get(clazz);
		if(!objMap.isEmpty()){
			return Lists.newArrayList((Collection<T>)objMap.values());
		}
		throw new IllegalStateException("Failed getInstance class(interface: " + clazz);
		
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> clazz, String key) {
		ConcurrentMap<String, Object> objMap = objectsCachedMap.get(clazz);
		if (objMap == null) {
			synchronized (clazz) {
				objMap = objectsCachedMap.get(clazz);
				if(objMap==null){
					objMap = Maps.newConcurrentMap();
					objectsCachedMap.put(clazz, objMap);
				}
			}
		}
		objMap = objectsCachedMap.get(clazz);

		T obj = (T) objMap.get(key);

		if (obj != null) {
			return obj;
		}
		
		//初始化所有
		initClazzInstances(clazz);
		
		obj = (T) objMap.get(key);

		if (obj != null) {
			return obj;
		}
		
		throw new IllegalStateException("Failed getInstance class(interface: " + clazz + ", key: " + key + ")");

	}
	
	private static <T> void initClazzInstances(Class<T> clazz){
		
		Map<String, Class<?>> clazzMap = getClazzMap(clazz);
		ConcurrentMap<String, Object> objMap = objectsCachedMap.get(clazz);
		if(objMap.isEmpty()){
			synchronized (clazz) {
				objMap = objectsCachedMap.get(clazz);
				if(objMap.isEmpty()){
					Iterator<Entry<String, Class<?>>> iter = clazzMap.entrySet().iterator();
					while (iter.hasNext()) {
						Entry<String, Class<?>> entry = iter.next();
					    String entryKey = entry.getKey();
					    Class<?> val = entry.getValue();
					    Object oldObj = objMap.get(entryKey);
					    if(oldObj==null){
						    Object newObj;
							try {
								newObj = val.newInstance();
								objMap.putIfAbsent(entryKey, newObj);
							} catch (Exception e) {
							}
					    }
					}
					objectsCachedMap.put(clazz, objMap);
				}
			}
		}
		
	}

	private static <T> Map<String, Class<?>> getClazzMap(Class<T> clazz) {
		Map<String, Class<?>> clazzMap = clazzCacheMap.get(clazz);
		if (clazzMap == null) {
			loadFile(clazz);
		}
		clazzMap = clazzCacheMap.get(clazz);
		return clazzMap;
	}

	private static void loadFile(Class<?> type) {
		String fileName = PREFIX + type.getName();
		Map<String, Class<?>> map = Maps.newHashMap();
		try {
			Enumeration<java.net.URL> urls;
			ClassLoader classLoader = ServiceContainer.class.getClassLoader();
			if (classLoader != null) {
				urls = classLoader.getResources(fileName);
			} else {
				urls = ClassLoader.getSystemResources(fileName);
			}
			if (urls != null) {
				while (urls.hasMoreElements()) {
					java.net.URL url = urls.nextElement();
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
						try {
							String line = null;
							while ((line = reader.readLine()) != null) {
								final int ci = line.indexOf('#');
								if (ci >= 0)
									line = line.substring(0, ci);
								line = line.trim();
								if (line.length() > 0) {
									try {
										String name = null;
										int i = line.indexOf('=');
										if (i > 0) {
											name = line.substring(0, i).trim();
											line = line.substring(i + 1).trim();
										}
										if (line.length() > 0) {
											Class<?> clazz = Class.forName(line, false, classLoader);
											if (!type.isAssignableFrom(clazz)) {
												throw new IllegalStateException("Error when load extension class(interface: " + type + ", class line: " + clazz.getName() + "), class "
														+ clazz.getName() + "is not subtype of interface.");
											}
											map.put(name, clazz);
										}
									} catch (Throwable t) {
									}
								}
							} // end of while read lines
						} finally {
							reader.close();
						}
					} catch (Throwable t) {
						log.error("", "Exception when load extension class(interface: " + type + ", class file: " + url + ") in " + url, t);
					}
				} // end of while urls
			}
		} catch (Throwable t) {
			log.error("", "Exception when load extension class(interface: " + type + ", description file: " + fileName + ").", t);
		}
		synchronized (type) {
			Map<String, Class<?>> oldMap = clazzCacheMap.get(type);
			if(oldMap==null){
				clazzCacheMap.put(type, map);
			}
		}
		
	}

	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

}
