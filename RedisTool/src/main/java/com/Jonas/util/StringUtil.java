package com.Jonas.util;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public final static String CRLF = "\r\n";

    public static String StringFilter(String str) {
        if (str == null) return str;
        Pattern p = Pattern.compile("[\0\1\2\3\4\6\7\u000B\u001C\u001D\u001E\u001F\u0014\u0018]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return m.replaceAll(" ").trim();
        } else {
            return str;
        }
    }

    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public static String IdToUpperCase(String s) {
        if (s == null)
            s = "";
        s = s.trim().toUpperCase();
        s = s.replaceAll(" ", "_");
        s = s.replaceAll("_+", "_");
        return s;
    }

    public static void append(StringBuilder buf, byte b, int base) {
        int bi = 0xff & b;
        int c = '0' + (bi / base) % base;
        if (c > '9')
            c = 'a' + (c - '0' - 10);
        buf.append((char) c);
        c = '0' + bi % base;
        if (c > '9')
            c = 'a' + (c - '0' - 10);
        buf.append((char) c);
    }

    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String getRandomString2(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String[] split(String s, String regex, int limit) {
        if (s == null) return new String[0];
        return s.split(regex, limit);
    }
}
