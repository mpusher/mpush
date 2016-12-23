package com.mpush.monitor.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MBeanRegistry {
    public static final String DOMAIN = "com.mpush";
    private static final Logger LOG = LoggerFactory.getLogger(MBeanRegistry.class);

    private static MBeanRegistry instance = new MBeanRegistry();

    private Map<MBeanInfo, String> mapBean2Path = new ConcurrentHashMap<>();

    private MBeanServer mBeanServer;

    public static MBeanRegistry getInstance() {
        return instance;
    }

    public MBeanRegistry() {
        try {
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        } catch (Error e) {
            // Account for running within IKVM and create a new MBeanServer
            // if the PlatformMBeanServer does not exist.
            mBeanServer = MBeanServerFactory.createMBeanServer();
        }
    }

    /**
     * Return the underlying MBeanServer that is being
     * used to register MBean's. The returned MBeanServer
     * may be a new empty MBeanServer if running through IKVM.
     */
    public MBeanServer getPlatformMBeanServer() {
        return mBeanServer;
    }

    /**
     * Registers a new MBean with the platform MBean server.
     *
     * @param bean   the bean being registered
     * @param parent if not null, the new bean will be registered as a child
     *               node of this parent.
     */
    public void register(MBeanInfo bean, MBeanInfo parent) {
        assert bean != null;
        String path = null;
        if (parent != null) {
            path = mapBean2Path.get(parent);
            assert path != null;
        }
        path = makeFullPath(path, parent);
        try {
            ObjectName name = makeObjectName(path, bean);
            mBeanServer.registerMBean(bean, name);
            mapBean2Path.put(bean, path);
        } catch (JMException e) {
            LOG.warn("Failed to register MBean " + bean.getName());
            throw new MException(e);
        }
    }

    /**
     * Unregister the MBean identified by the path.
     *
     * @param path
     * @param bean
     */
    private void unregister(String path, MBeanInfo bean) {
        if (path == null) return;
        try {
            mBeanServer.unregisterMBean(makeObjectName(path, bean));
        } catch (JMException e) {
            LOG.warn("Failed to unregister MBean " + bean.getName());
            throw new MException(e);
        }
    }

    /**
     * Unregister MBean.
     *
     * @param bean
     */
    public void unregister(MBeanInfo bean) {
        if (bean == null) return;

        String path = mapBean2Path.get(bean);
        unregister(path, bean);
        mapBean2Path.remove(bean);
    }

    /**
     * Unregister all currently registered MBeans
     */
    public void unregisterAll() {
        for (Map.Entry<MBeanInfo, String> e : mapBean2Path.entrySet()) {
            try {
                unregister(e.getValue(), e.getKey());
            } catch (MException e1) {
                LOG.warn("Error during unregister", e1);
            }
        }
        mapBean2Path.clear();
    }

    /**
     * Generate a filesystem-like path.
     *
     * @param prefix path prefix
     * @param name   path elements
     * @return absolute path
     */
    public String makeFullPath(String prefix, String... name) {
        StringBuilder sb = new StringBuilder(prefix == null ? "/" : (prefix.equals("/") ? prefix : prefix + "/"));
        boolean first = true;
        for (String s : name) {
            if (s == null) continue;
            if (!first) {
                sb.append("/");
            } else
                first = false;
            sb.append(s);
        }
        return sb.toString();
    }

    protected String makeFullPath(String prefix, MBeanInfo bean) {
        return makeFullPath(prefix, bean == null ? null : bean.getName());
    }

    /**
     * This takes a path, such as /a/b/c, and converts it to
     * name0=a,name1=b,name2=c
     */
    private int tokenize(StringBuilder sb, String path, int index) {
        String[] tokens = path.split("/");
        for (String s : tokens) {
            if (s.length() == 0)
                continue;
            sb.append("name").append(index++)
                    .append("=").append(s).append(",");
        }
        return index;
    }

    /**
     * Builds an MBean path and creates an ObjectName instance using the path.
     *
     * @param path MBean path
     * @param bean the MBean instance
     * @return ObjectName to be registered with the platform MBean server
     */
    protected ObjectName makeObjectName(String path, MBeanInfo bean)
            throws MalformedObjectNameException {
        if (path == null)
            return null;
        StringBuilder beanName = new StringBuilder(DOMAIN).append(':');
        int counter = 0;
        counter = tokenize(beanName, path, counter);
        tokenize(beanName, bean.getName(), counter);
        beanName.deleteCharAt(beanName.length() - 1);
        try {
            return new ObjectName(beanName.toString());
        } catch (MalformedObjectNameException e) {
            LOG.warn("Invalid name \"" + beanName.toString() + "\" for class "
                    + bean.getClass().toString());
            throw e;
        }
    }
}