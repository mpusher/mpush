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

import com.mpush.api.Constants;
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
 *
 * @author ohun@live.cn
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

        Profiler.enter("time cost on [compress]");

        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length / 4);
        DeflaterOutputStream zipOut = new DeflaterOutputStream(out);
        try {
            zipOut.write(data);
            zipOut.finish();
            zipOut.close();
        } catch (IOException e) {
            LOGGER.error("compress ex", e);
            return Constants.EMPTY_BYTES;
        } finally {
            close(zipOut);
            Profiler.release();
        }
        return out.toByteArray();
    }

    public static byte[] decompress(byte[] data) {
        Profiler.enter("time cost on [decompress]");
        InflaterInputStream zipIn = new InflaterInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = zipIn.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            LOGGER.error("decompress ex", e);
            return Constants.EMPTY_BYTES;
        } finally {
            close(zipIn);
            Profiler.release();
        }
        return out.toByteArray();
    }
}
