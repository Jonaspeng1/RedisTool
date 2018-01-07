package com.Jonas.redis;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Jonas.util.PropertiesUtil;
import com.Jonas.util.StringUtil;
import com.Jonas.util.ThreadLocalLog;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class XedisClient {

    private static final Logger logger = LoggerFactory.getLogger(XedisClient.class);
    private static Map<String, InnerXedisClient> xedisClientMap = new HashMap<String, InnerXedisClient>();
    private static Lock lock = new ReentrantLock();

    private static class InnerXedisClient {
        private String type;
        private BlockingQueue<Xedis> pool;
        private Map<Xedis, JedisPubSub> subscribing;
        private int maxConn = 5000;
        private AtomicInteger xedisCount = new AtomicInteger();
        private XedisConfig config;
        private Lock lock = new ReentrantLock();

        InnerXedisClient(String type, XedisConfig config) {
            this.type = type;
            this.config = config;
            this.maxConn = config.getMaxConn();
            pool = new LinkedBlockingQueue<Xedis>(maxConn);
            subscribing = new HashMap<Xedis, JedisPubSub>();

            logger.info("{} Redis Info: {}", StringUtils.isEmpty(type) ? "<default>" : type, config);
        }

        String getInfo() {
            if (config != null) {
                return config.getHost() + ":" + config.getPort();
            }
            return "";
        }

        String getType() {
            return type;
        }

        XedisConfig getConfig() {
            return config;
        }

        void setConfig(XedisConfig config) {
            if (this.config.equals(config)) return;
            this.config = config;
            logger.info("{} Change Redis Info: {}", StringUtils.isEmpty(type) ? "<default>" : type, config);
        }

        void beforeSubscribe(Xedis xedis, JedisPubSub jedisPubSub) {
            subscribing.put(xedis, jedisPubSub);
        }

        void afterSubscribe(Xedis xedis, JedisPubSub jedisPubSub) {
            subscribing.remove(xedis);
        }

        Xedis getClient() {
            long begin = System.currentTimeMillis();
            boolean createNew = false;
            try {
                Xedis xedis;
                try {
                    int count = xedisCount.get();
                    int l = count / 10;
                    if (l < 20 && count < maxConn) {
                        xedis = pool.poll();
                    } else if (l < 100 && count < maxConn) {
                        xedis = pool.poll(l * 2, TimeUnit.MILLISECONDS);
                    } else if (count < maxConn - 1) {
                        xedis = pool.poll(500L, TimeUnit.MILLISECONDS);
                    } else {
                        xedis = pool.take();
                    }
                    if (xedis != null) {
                        if (!config.equals(xedis.getConfig())) {
                            xedis.disconnect();
                            xedisCount.decrementAndGet();
                            xedis = null;
                        }
                    }
                } catch (InterruptedException ignore) {
                    xedis = null;
                }
                if (xedis == null) {
                    createNew = true;
                    xedis = new Xedis(getType(), config);
                    xedisCount.incrementAndGet();
                }
                return xedis;
            } finally {
                long t = System.currentTimeMillis() - begin;
                if (t > 20) {
                    String s = "Xedis.getClient cost:" + t + (createNew ? " create new client)" : "");
                    if (!ThreadLocalLog.addLog(s)) {
                        LoggerFactory.getLogger("Slow").info(s);
                    }
                }
            }
        }

        void closeAll() {
            if (!subscribing.isEmpty()) {
                try {
                    List<Map.Entry<Xedis, JedisPubSub>> list = new ArrayList<Map.Entry<Xedis, JedisPubSub>>(subscribing.entrySet());
                    for (Map.Entry<Xedis, JedisPubSub> entry : list) {
                        Xedis xedis = entry.getKey();
                        JedisPubSub jedisPubSub = entry.getValue();
                        XedisConfig oldConfig = xedis.getConfig();
                        if (config.equals(oldConfig)) continue;
                        jedisPubSub.unsubscribe();
                    }
                } catch (Throwable ignore) {
                }
            }

            int size = pool.size();
            for (int i = 0; i < size; i++) {
                Xedis xedis = pool.poll();
                if (xedis == null) break;
                XedisConfig oldConfig = xedis.getConfig();
                if (config.equals(oldConfig)) break;

                xedis.disconnect();
                xedisCount.decrementAndGet();
            }
        }

        void returnClient(Xedis j) {
            if (j == null) return;
            boolean closeClient = false;
            long begin = System.currentTimeMillis();
            if (xedisCount.get() + 100 > maxConn) {
                closeClient = true;
                j.disconnect();
                xedisCount.decrementAndGet();
            } else {
                XedisConfig oldConfig = j.getConfig();
                if (!config.equals(oldConfig)) {
                    closeClient = true;
                    j.disconnect();
                    xedisCount.decrementAndGet();
                } else if (!pool.offer(j)) {
                    closeClient = true;
                    j.disconnect();
                    xedisCount.decrementAndGet();
                }
            }
            long t = System.currentTimeMillis() - begin;
            if (t > 20) {
                String s = "Xedis.returnClient cost:" + t + (closeClient ? " close client)" : "");
                if (!ThreadLocalLog.addLog(s)) {
                    LoggerFactory.getLogger("Slow").info(s);
                }
            }
        }

        public int getXedisCount() {
            return xedisCount.get();
        }

        public int getMaxConn() {
            return maxConn;
        }
    }

    private static XedisConfig getXedisConfig(String type) {
        Properties properties = PropertiesUtil.loadProperties("config/redis.properties");

        String defaultKeyPref = "Jonas.";
        String keyPref = defaultKeyPref;
        if (StringUtils.isNotEmpty(type)) {
            keyPref = defaultKeyPref + type + ".";
        }

        String masterName = properties.getProperty(keyPref + "redis.masterName", "");
        String sentinelList = properties.getProperty(keyPref + "redis.sentinel", "");
        String host = properties.getProperty(keyPref + "redis.host", "");
        int port = Integer.parseInt(properties.getProperty(keyPref + "redis.port", "6379"));
        int timeout = Integer.parseInt(properties.getProperty(keyPref + "redis.timeout", "2000"));
        String password = properties.getProperty(keyPref + "redis.password", "");
        int maxConn = Integer.parseInt(properties.getProperty(keyPref + "redis.pool", "5000"));

        if (StringUtils.isNotEmpty(masterName) && StringUtils.isNotEmpty(sentinelList)) {
            return initSentinel(type, masterName, sentinelList, password, timeout, maxConn);
        } else {
            if (StringUtils.isEmpty(host)) {
                if (StringUtils.isEmpty(type))
                    host = "127.0.0.1";
                else
                    return null;
            }

            XedisConfig config = new XedisConfig();
            config.setHost(host);
            config.setPort(port);
            config.setTimeout(timeout);
            config.setPassword(password);
            config.setMaxConn(maxConn);

            return config;
        }
    }

    private static XedisConfig initSentinel(String type, String masterName, String sentinelList, String password, int timeout, int maxConn) {
        String[] sentinels = sentinelList.split(",");
        XedisConfig masterConfig = null;
        for (String sentinel : sentinels) {
            String[] s = sentinel.split(":");
            String host = s[0];
            int port = 6379;
            try {
                port = Integer.parseInt(s[1]);
            } catch (Throwable ignore) {
            }
            Jedis jedis = null;
            try {
                jedis = new Jedis(host, port);
                if (masterConfig == null) {
                    List<String> strings = jedis.sentinelGetMasterAddrByName(masterName);
                    String masterHost = strings.get(0);
                    int masterPort = Integer.parseInt(strings.get(1));
                    masterConfig = new XedisConfig();
                    masterConfig.setHost(masterHost);
                    masterConfig.setPort(masterPort);
                    masterConfig.setTimeout(timeout);
                    masterConfig.setPassword(password);
                    masterConfig.setMaxConn(maxConn);
                    logger.info("Found Redis master at {}", masterConfig);
                    break;
                }
            } catch (JedisConnectionException e) {
                logger.warn("Cannot connect to sentinel running @ {}:{}. Trying next one.", host, port);
            } finally {
                try {
                    if (jedis != null) jedis.quit();
                } catch (Exception ignore) {
                }
                try {
                    if (jedis != null) jedis.disconnect();
                } catch (Exception ignore) {
                }
            }
        }

        if (masterConfig != null) {
            logger.info("Redis master running at {}, starting Sentinel listeners...", masterConfig);

            for (String sentinel : sentinels) {
                String[] s = sentinel.split(":");
                String host = s[0];
                int port = 6379;
                try {
                    port = Integer.parseInt(s[1]);
                } catch (Throwable ignore) {
                }
                XedisSentinelMasterListener masterListener = new XedisSentinelMasterListener(type, masterName, host, port);
                masterListener.start();
            }
        } else {
            logger.error("All sentinels down, cannot determine where is {} master is running...", masterName);
            throw new RuntimeException("can not get xedis for " + type);
        }

        return masterConfig;
    }

    private static InnerXedisClient getInnerXedisClient(String type) {
        if (type == null) type = "";
        InnerXedisClient xedisClient = xedisClientMap.get(type);
        if (xedisClient == null) {
            lock.lock();
            try {
                xedisClient = xedisClientMap.get(type);
                if (xedisClient == null) {
                    XedisConfig config = getXedisConfig(type);
                    if (config != null) {
                        xedisClient = new InnerXedisClient(type, config);
                        xedisClientMap.put(type, xedisClient);
                    }
                }
            } finally {
                lock.unlock();
            }
            if (xedisClient == null && StringUtils.isNotEmpty(type)) {
                xedisClient = getInnerXedisClient("");
                if (xedisClient != null) {
                    xedisClientMap.put(type, xedisClient);
                }
            }
        }
        return xedisClient;
    }

    static Xedis getClient(String type) {
        InnerXedisClient innerXedisClient = getInnerXedisClient(type);
        if (innerXedisClient == null)
            throw new RuntimeException("can not get xedis for " + type);
        return innerXedisClient.getClient();
    }

    static void updateClient(String type, String host, int port) {
        if (type == null) type = "";
        InnerXedisClient xedisClient = xedisClientMap.get(type);
        if (xedisClient != null) {
            xedisClient.lock.lock();
            try {
                XedisConfig config = xedisClient.getConfig();
                XedisConfig newConfig = new XedisConfig();
                newConfig.setTimeout(config.getTimeout());
                newConfig.setPassword(config.getPassword());
                newConfig.setMaxConn(config.getMaxConn());
                newConfig.setHost(host);
                newConfig.setPort(port);
                xedisClient.setConfig(newConfig);
                xedisClient.closeAll();
            } finally {
                xedisClient.lock.unlock();
            }
        }
    }

    static void returnClient(Xedis j) {
        if (j == null) return;
        getInnerXedisClient(j.getType()).returnClient(j);
    }

    static void beforeSubscribe(String type, Xedis xedis, JedisPubSub jedisPubSub) {
        if (xedis == null) return;
        InnerXedisClient xedisClient = xedisClientMap.get(type);
        if (xedisClient != null) {
            xedisClient.beforeSubscribe(xedis, jedisPubSub);
        }
    }

    static void afterSubscribe(String type, Xedis xedis, JedisPubSub jedisPubSub) {
        if (xedis == null) return;
        InnerXedisClient xedisClient = xedisClientMap.get(type);
        if (xedisClient != null) {
            xedisClient.afterSubscribe(xedis, jedisPubSub);
        }
    }
    public static String getXedisInfo(String type) {
        InnerXedisClient innerXedisClient = getInnerXedisClient(type);
        if (innerXedisClient == null) return "";
        else return innerXedisClient.getInfo();
    }
    
    
    public static String getXedisCountInfo() {
        StringBuilder sb = new StringBuilder();
        Map<InnerXedisClient, String> data = new HashMap<InnerXedisClient, String>();
        for (Map.Entry<String, InnerXedisClient> entry : xedisClientMap.entrySet()) {
            InnerXedisClient xedisClient = entry.getValue();
            String type = entry.getKey();
            if (StringUtils.isEmpty(type)) {
                type = "<default>";
            }
            String s = data.get(xedisClient);
            if (s == null) s = type;
            else s = s + "," + type;
            data.put(xedisClient, s);
        }
        for (Map.Entry<InnerXedisClient, String> entry : data.entrySet()) {
            InnerXedisClient xedisClient = entry.getKey();
            String type = entry.getValue();
            sb.append(type);
            sb.append("(").append(xedisClient.getInfo()).append(")");
            sb.append(":").append(xedisClient.getXedisCount()).append("/").append(xedisClient.getMaxConn()).append(StringUtil.CRLF);
        }
        return sb.toString();
    }

    public static int getDefaultXedisCount() {
        InnerXedisClient xedisClient = xedisClientMap.get("");
        if (xedisClient != null) return xedisClient.getXedisCount();
        return 0;
    }

    public static int getDefaultXedisMaxConn() {
        InnerXedisClient xedisClient = xedisClientMap.get("");
        if (xedisClient != null) return xedisClient.getMaxConn();
        return 0;
    }

    public static String genKey(Object... keys) {
        return StringUtils.join(keys, ":");
    }
}
