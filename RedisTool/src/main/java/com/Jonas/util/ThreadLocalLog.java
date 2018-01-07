package com.Jonas.util;

/**
 * User: Janon
 * Date: 14-5-7 上午10:06
 */
public class ThreadLocalLog {
    private static ThreadLocal<StringBuilder> threadLocalLog = new ThreadLocal<StringBuilder>();
    private static ThreadLocal<Boolean> threadLocalLogable = new ThreadLocal<Boolean>();

    public static void init() {
        threadLocalLogable.set(true);
        threadLocalLog.set(null);
    }

    public static boolean addLog(String... logs) {
        Boolean flag = threadLocalLogable.get();
        return addLog(flag != null && flag, logs);
    }

    private static boolean addLog(boolean flag, String... logs) {
        StringBuilder sb = threadLocalLog.get();
        if (sb == null) {
            if (!flag) return false;
            sb = new StringBuilder();
            threadLocalLog.set(sb);
        }
        if (logs.length > 0) {
            if (sb.length() > 0)
                sb.append("\n");
            sb.append("    ");
            for (String log : logs) {
                sb.append(log).append(" ");
            }
        }
        return true;
    }

    public static String getLog() {
        StringBuilder stringBuilder = threadLocalLog.get();
        String rtn = "";
        if (stringBuilder != null) {
            rtn = stringBuilder.toString();
        }
        return rtn;
    }

    public static void clear() {
        threadLocalLogable.remove();
        threadLocalLog.remove();
    }
}
