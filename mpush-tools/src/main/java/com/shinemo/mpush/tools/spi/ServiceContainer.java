package com.shinemo.mpush.tools.spi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ServiceContainer {

	private static final Logger log = LoggerFactory.getLogger(ServiceContainer.class);
	private static final String PREFIX = "META-INF/mpush/services/";
	
	private static final ConcurrentMap<Class<?>, Object> objectCacheMap = Maps.newConcurrentMap();
	private final static ConcurrentHashMap<Class<?>, Map<String, Class<?>>> clazzCacheMap = new ConcurrentHashMap<Class<?>, Map<String, Class<?>>>();
	private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Object>> objectsCachedMap = Maps.newConcurrentMap();
	
	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> clazz){
		T instance =  (T)objectCacheMap.get(clazz);
		if(instance == null){
			try{
				instance = ServiceLoader.load(clazz,ServiceContainer.class.getClassLoader()).iterator().next();
				objectCacheMap.putIfAbsent(clazz, instance);
				return (T) objectCacheMap.get(clazz);
			}catch(Throwable e){
				log.warn("can not load "+ clazz,e);
			}
		}else{
			if(clazz.isAssignableFrom(instance.getClass())){
				return instance;
			}else{
				log.warn("[ init service error:]"+clazz);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> getInstances(Class<T> clazz){
		List<T> ret = (List<T>)objectCacheMap.get(clazz);
		if(ret == null){
			try{
				ret = Lists.newArrayList();
				for(T instance:ServiceLoader.load(clazz, ServiceContainer.class.getClassLoader())){
					ret.add(instance);
				}
				objectCacheMap.putIfAbsent(clazz, ret);
				return (List<T>)objectCacheMap.get(clazz);
			}catch(Throwable e){
				log.warn("can not load "+ clazz,e);
			}
		}else{
			if(List.class.isAssignableFrom(ret.getClass())){
				return ret;
			}else{
				log.warn("[ init service error:]"+clazz);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> clazz,String key){
		ConcurrentMap<String, Object> objMap = objectsCachedMap.get(clazz);
		if(objMap == null){
			objMap = Maps.newConcurrentMap();
			objectsCachedMap.put(clazz, objMap);
		}
		objMap = objectsCachedMap.get(clazz);
		
		T obj = (T) objMap.get(key);
		
		if(obj != null){
			return obj;
		}
		
		Map<String,Class<?>> clazzMap = clazzCacheMap.get(clazz);
		if(clazzMap == null){
			loadFile(clazz);
		}
		clazzMap = clazzCacheMap.get(key);
		if(clazz != null){
			try{
				Object newObj = clazz.newInstance();
				Object preObj = objMap.putIfAbsent(key, newObj);
				return null == preObj?(T)newObj:(T)preObj;
			}catch(Exception e){
				log.warn("[ getInstance ] error:"+clazz+","+key,e);
			}
		}
		
		return null;
		
	}
	
	private static void loadFile(Class<?> type){
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
                                                throw new IllegalStateException(
                                                        "Error when load extension class(interface: " + type
                                                                + ", class line: " + clazz.getName() + "), class "
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
                        log.error("", "Exception when load extension class(interface: " + type + ", class file: "
                                + url + ") in " + url, t);
                    }
                } // end of while urls
            }
        } catch (Throwable t) {
            log.error("", "Exception when load extension class(interface: " + type + ", description file: "
                    + fileName + ").", t);
        }
        clazzCacheMap.put(type, map);
	}
	
}
