package com.mpush.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yxx on 2016/5/15.
 *
 * @author ohun@live.cn
 */
public final class ConsoleLog {
    private static final String TAG = "[mpush] ";
    private static final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void d(String s, Object... args) {
        System.out.printf(format.format(new Date()) + " [D] " + TAG + s + '\n', args);
    }

    public static void i(String s, Object... args) {
        System.out.printf(format.format(new Date()) + " [I] " + TAG + s + '\n', args);
    }

    public static void w(String s, Object... args) {
        System.err.printf(format.format(new Date()) + " [W] " + TAG + s + '\n', args);
    }

    public static void e(Throwable e, String s, Object... args) {
        System.err.printf(format.format(new Date()) + " [E] " + TAG + s + '\n', args);
        e.printStackTrace();
    }
}
