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

package com.mpush.tools.common;

import com.sun.management.HotSpotDiagnosticMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class JVMUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JVMUtil.class);

    private static final String HOT_SPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
    private static volatile HotSpotDiagnosticMXBean mxBean;

    private static Object lock = new Object();

    public static void jstack(OutputStream stream) throws Exception {
        try {
            Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
            Iterator<Map.Entry<Thread, StackTraceElement[]>> ite = map.entrySet().iterator();
            while (ite.hasNext()) {
                Map.Entry<Thread, StackTraceElement[]> entry = ite.next();
                StackTraceElement[] elements = entry.getValue();
                if (elements != null && elements.length > 0) {
                    String threadName = entry.getKey().getName();
                    stream.write(("Thread Name :[" + threadName + "]\n").getBytes());
                    for (StackTraceElement el : elements) {
                        String stack = el.toString() + "\n";
                        stream.write(stack.getBytes());
                    }
                    stream.write("\n".getBytes());
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void dumpJstack(final String jvmPath) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String logPath = jvmPath;
                FileOutputStream jstackStream = null;
                try {
                    jstackStream = new FileOutputStream(new File(logPath, System.currentTimeMillis() + "-jstack.LOGGER"));
                    JVMUtil.jstack(jstackStream);
                } catch (Throwable t) {
                    LOGGER.error("Dump JVM cache Error!", t);
                } finally {
                    if (jstackStream != null) {
                        try {
                            jstackStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        });
    }

    private static HotSpotDiagnosticMXBean getMxBean() {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<HotSpotDiagnosticMXBean>() {
                public HotSpotDiagnosticMXBean run() throws Exception {
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                    Set<ObjectName> s = server.queryNames(new ObjectName(HOT_SPOT_BEAN_NAME), null);
                    Iterator<ObjectName> itr = s.iterator();
                    if (itr.hasNext()) {
                        ObjectName name = itr.next();
                        HotSpotDiagnosticMXBean bean = ManagementFactory.newPlatformMXBeanProxy(server,
                                name.toString(), HotSpotDiagnosticMXBean.class);
                        return bean;
                    } else {
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("getMxBean Error!", e);
            return null;
        }
    }

    private static void initHotspotMBean() throws Exception {
        if (mxBean == null) {
            synchronized (lock) {
                if (mxBean == null) {
                    mxBean = getMxBean();
                }
            }
        }
    }

    public static void jMap(String fileName, boolean live) {
        File f = new File(fileName, System.currentTimeMillis() + "-jmap.LOGGER");
        String currentFileName = f.getPath();
        try {
            initHotspotMBean();
            if (f.exists()) {
                f.delete();
            }
            mxBean.dumpHeap(currentFileName, live);
        } catch (Exception e) {
            LOGGER.error("dumpHeap Error!" + currentFileName, e);
        }
    }

    public static void dumpJmap(final String jvmPath) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                jMap(jvmPath, false);
            }
        });
    }

}
