package com.Jonas.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Jonas.util.ThreadLocalLog;

import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * WrapXedisInvokerInvocationHandler
 * Created by Janon on 2015/9/25.
 */
public class WrapXedisInvokerInvocationHandler implements InvocationHandler {
    private final static Logger logger = LoggerFactory.getLogger(WrapXedisInvokerInvocationHandler.class);
    private String type;

    WrapXedisInvokerInvocationHandler(String type) {
        this.type = type;
    }

    protected Xedis getClient() {
        return XedisClient.getClient(type);
    }

    protected void returnClient(Xedis client) {
        XedisClient.returnClient(client);
    }

    private static Map<String, Method> methodCache = new HashMap<String, Method>();
    private static Lock lock = new ReentrantLock();

    private static Method getMethod(String methodName, Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName).append("(");
        if (parameterTypes != null) {
            for (Class<?> clazz : parameterTypes) {
                sb.append(clazz.getName()).append(",");
            }
        }
        sb.append(")");
        String cacheKey = sb.toString();
        Method method = methodCache.get(cacheKey);
        if (method == null) {
            lock.lock();
            try {
                method = methodCache.get(cacheKey);
                if (method == null) {
                    try {
                        method = Xedis.class.getMethod(methodName, parameterTypes);
                        methodCache.put(cacheKey, method);
                    } catch (Throwable ignore) {
                        logger.warn(ignore.getMessage(), ignore);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return method;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        if ("subscribe".equals(methodName)) {
            subscribe((JedisPubSub) args[0], (String[]) args[1]);
            return null;
        } else if ("pipelined".equals(methodName)) {
            return new WrapPipeline(type);
        } else if ("getXedisInfo".equals(methodName)) {
            return XedisClient.getXedisInfo(type);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean isQuiet = false;
        if (methodName.endsWith("Quiet")) {
            isQuiet = true;
            methodName = methodName.substring(0, methodName.length() - 5);
        }
        Method executeMethod = getMethod(methodName, parameterTypes);
        if (executeMethod != null) {
            Xedis client = null;
            long begin = System.currentTimeMillis();
            boolean isError = false;
            try {
                client = getClient();
                return executeMethod.invoke(client, args);
            } catch (Throwable ex) {
                isError = true;
                checkException(client, isQuiet, ex);
            } finally {
                returnClient(client);
                long t = System.currentTimeMillis() - begin;
                if (t > 10) {
                    String key = null;
                    int keyCount = -1;
                    int fieldCount = -1;
                    if (args.length > 0) {
                        if (args[0] instanceof String) {
                            key = (String) args[0];
                            keyCount = 1;
                        } else if (args[0] instanceof String[]) {
                            keyCount = ((String[]) args[0]).length;
                        }
                    }
                    if (args.length > 1) {
                        if (args[1] instanceof String[]) {
                            fieldCount = ((String[]) args[1]).length;
                        } else if (args[1] instanceof Map) {
                            fieldCount = ((Map) args[1]).size();
                        }
                    }
                    ThreadLocalLog.addLog("--", "Xedis." + methodName, key != null ? "key:" + args[0] + (fieldCount > 0 ? " fieldCount:" + fieldCount : "") : (keyCount > 0 ? "keyCount:" + keyCount : ""), "success:" + (!isError), "cost:" + t);
                }
            }
        } else {
            throw new IllegalAccessException(methodName + " not found!");
        }
        return null;
    }

    private void checkException(Xedis client, boolean isQuiet, Throwable ex) throws Throwable {
        if (ex instanceof InvocationTargetException) {
            ex = ((InvocationTargetException) ex).getTargetException();
        }
        if (ex instanceof JedisConnectionException
                || ex instanceof ClassCastException) {
            if (client != null) client.setNeedGetNew(true);
        }
        if (!isQuiet) throw ex;
    }

    private void subscribe(JedisPubSub jedisPubSub, String... channels) {
        Xedis client = null;
        try {
            client = getClient();
            logger.debug("begin subscribe {} at {}", channels, client.getConfig());
            XedisClient.beforeSubscribe(type, client, jedisPubSub);
            client.subscribe(jedisPubSub, channels);
        } catch (JedisConnectionException ex) {
            if (client != null) client.setNeedGetNew(true);
            throw ex;
        } catch (ClassCastException ex) {
            if (client != null) client.setNeedGetNew(true);
            throw ex;
        } finally {
            logger.debug("end subscribe {} at {}", channels, client == null ? "<null>" : client.getConfig());
            XedisClient.afterSubscribe(type, client, jedisPubSub);
            returnClient(client);
        }
    }
}
