package com.shinemo.mpush.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JVMUtil {
	
	private static final Logger log = LoggerFactory.getLogger(JVMUtil.class);
	
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
	
	public static void dumpJstack(final String jvmPath){
		Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String logPath = jvmPath;
                FileOutputStream jstackStream = null;
                try {
                    jstackStream = new FileOutputStream(new File(logPath, "jstack.log"));
                    JVMUtil.jstack(jstackStream);
                } catch (FileNotFoundException e) {
                	log.error("", "Dump JVM cache Error!", e);
                } catch (Throwable t) {
                	log.error("", "Dump JVM cache Error!", t);
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

}
