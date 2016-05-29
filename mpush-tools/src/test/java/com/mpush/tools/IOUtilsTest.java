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

package com.mpush.tools;

import com.mpush.tools.common.IOUtils;
import org.junit.Test;

/**
 * Created by ohun on 2016/3/8.
 *
 * @author ohun@live.cn
 */
public class IOUtilsTest {

    @Test
    public void testCompress() throws Exception {
        byte[] s = ("士大夫士大大啊实打实大苏打撒" +
                "阿斯顿撒大苏打实打实的苏打似的啊实" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "打实大苏打实打实大苏打水水" +
                "水水水水夫").getBytes();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            IOUtils.compress(s);
        }
        System.out.println((System.currentTimeMillis() - t1) / 100);
        System.out.println("src:" + s.length);
        byte[] out = IOUtils.compress(s);
        System.out.println("compress:" + out.length);
        byte[] ss = IOUtils.decompress(out);
        System.out.println("decompress:" + ss.length);
    }

    @Test
    public void testUncompress() throws Exception {

    }
}