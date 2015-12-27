package com.shinemo.mpush.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

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


    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DeflaterOutputStream zipOut = new DeflaterOutputStream(byteStream);
        try {
            zipOut.write(data);
            zipOut.finish();
            zipOut.close();
        } catch (IOException e) {
            LOGGER.error("compress ex", e);
            return Constants.EMPTY_BYTES;
        } finally {
            close(zipOut);
        }
        return byteStream.toByteArray();
    }

    public static byte[] uncompress(byte[] data) {
        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[data.length * 2];
        int length;
        try {
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            LOGGER.error("uncompress ex", e);
            return Constants.EMPTY_BYTES;
        } finally {
            close(in);
        }
        return out.toByteArray();
    }
}
