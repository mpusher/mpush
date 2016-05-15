package com.mpush.tools.spi;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceContainer {
    private static final Map<String, Object> cache = new ConcurrentHashMap<>();

    public static <T> T load(Class<T> clazz) {
        String name = clazz.getName();
        Object o = cache.get(name);
        if (o == null) {
            T t = load0(clazz);
            if (t != null) {
                cache.put(name, t);
                return t;
            }
        } else if (clazz.isInstance(o)) {
            return (T) o;
        }

        return load0(clazz);
    }

    public static <T> T load0(Class<T> clazz) {
        ServiceLoader<T> factories = ServiceLoader.load(clazz);
        if (factories.iterator().hasNext()) {
            return factories.iterator().next();
        } else {
            // By default ServiceLoader.load uses the TCCL, this may not be enough in environment deading with
            // classloaders differently such as OSGi. So we should try to use the  classloader having loaded this
            // class. In OSGi it would be the bundle exposing vert.x and so have access to all its classes.
            factories = ServiceLoader.load(clazz, ServiceContainer.class.getClassLoader());
            if (factories.iterator().hasNext()) {
                return factories.iterator().next();
            } else {
                throw new IllegalStateException("Cannot find META-INF/services/" + clazz.getName() + " on classpath");
            }
        }
    }
}
