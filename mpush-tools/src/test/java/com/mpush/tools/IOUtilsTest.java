package com.mpush.tools;

import org.junit.Test;

/**
 * Created by ohun on 2016/3/8.
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
        System.out.println((System.currentTimeMillis()-t1)/100);
        System.out.println("src:" + s.length);
        byte[] out = IOUtils.compress(s);
        System.out.println("compress:" + out.length);
        byte[] ss = IOUtils.uncompress(out);
        System.out.println("uncompress:" + ss.length);
    }

    @Test
    public void testUncompress() throws Exception {

    }
}