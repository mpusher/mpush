package com.shinemo.mpush.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * Created by ohun on 2015/12/25.
 */
public final class IOUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOGGER.error("close closeable ex", e);
            }
        }
    }
}
