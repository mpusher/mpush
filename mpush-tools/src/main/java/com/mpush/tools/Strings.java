package com.mpush.tools;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class Strings {
    public static final String EMPTY = "";

    public static boolean isBlank(CharSequence text) {
        if (text == null || text.length() == 0) return true;
        for (int i = 0, L = text.length(); i < L; i++) {
            if (!Character.isWhitespace(text.charAt(i))) return false;
        }
        return true;
    }

    public static String trimAll(CharSequence s) {
        if (s == null || s.length() == 0) return Strings.EMPTY;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0, L = s.length(); i < L; i++) {
            char c = s.charAt(i);
            if (c != ' ') sb.append(c);
        }
        return sb.toString();
    }
}
