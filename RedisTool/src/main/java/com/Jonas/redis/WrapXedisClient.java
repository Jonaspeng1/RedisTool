package com.Jonas.redis;

import java.lang.reflect.Proxy;

/**
 * User: Janon
 * Date: 14-1-20 下午4:38
 */
public class WrapXedisClient {
    private static WrapXedis createWrapXedis(String type) {
        return (WrapXedis) Proxy.newProxyInstance(WrapXedisClient.class.getClassLoader(), new Class[]{WrapXedis.class}, new WrapXedisInvokerInvocationHandler(type));
    }

    private static WrapXedis wrapXedis = createWrapXedis("");
    private static WrapXedis wrapQueueXedis = createWrapXedis("queue");
    private static WrapXedis wrapCacheXedis = createWrapXedis("cache");
    private static WrapXedis wrapPubXedis = createWrapXedis("pub");

    public static WrapXedis myClient() {
        if (wrapXedis == null)
            wrapXedis = createWrapXedis("");
        return wrapXedis;
    }

    public static WrapXedis myCacheClient() {
        if (wrapCacheXedis == null)
            wrapCacheXedis = createWrapXedis("cache");
        return wrapCacheXedis;
    }

    public static WrapXedis myQueueClient() {
        if (wrapQueueXedis == null)
            wrapQueueXedis = createWrapXedis("queue");
        return wrapQueueXedis;
    }

    public static WrapXedis myPubClient() {
        if (wrapPubXedis == null)
            wrapPubXedis = createWrapXedis("pub");
        return wrapPubXedis;
    }
}
