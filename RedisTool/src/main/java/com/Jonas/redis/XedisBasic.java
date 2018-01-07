package com.Jonas.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Jonas.util.ThreadLocalLog;

import redis.clients.jedis.Jedis;

class XedisBasic {
    private static final Logger logger = LoggerFactory.getLogger(XedisBasic.class);
    private final static long pingBetweenEvictionRunsMillis = 10000L;
    private XedisConfig config;
    private Jedis node;
    private long last = 0;
    private boolean needGetNew = false;

    public XedisBasic(XedisConfig config) {
        this.config = config;
        this.node = createResource();
    }

    public Jedis createResource() {
        Jedis jedis = new Jedis(config.getHost(), config.getPort(), config.getTimeout());
        String password = config.getPassword();
        if (password != null && password.length() > 0) {
            try {
                jedis.auth(password);
            } catch (redis.clients.jedis.exceptions.JedisDataException ex) {
                // auth异常时需要关闭连接，否则会连接泄漏  by zhanhong_deng 2015-01-15 09:47
                try {
                    jedis.quit();
                } catch (Exception ignore) {
                }
                try {
                    jedis.disconnect();
                } catch (Exception ignore) {
                }
                throw ex;
            }
        }
        return jedis;
    }

    public boolean isNeedGetNew() {
        return needGetNew;
    }

    public void setNeedGetNew(boolean needGetNew) {
        this.needGetNew = needGetNew;
    }

    public void disconnect() {
        if (node != null) {
            try {
                node.quit();
            } catch (Exception ignore) {
            }
            try {
                node.disconnect();
            } catch (Exception ignore) {
            }
        }
        this.node = null;
    }

    Jedis ping(Jedis node) {
        boolean getNew = isNeedGetNew();
        setNeedGetNew(false);
        if (!getNew) {
            long begin = System.currentTimeMillis();
            try {
                if (System.currentTimeMillis() - last < pingBetweenEvictionRunsMillis) {
                    last = System.currentTimeMillis();
                    return node;
                }
                if ("PONG".equals(node.ping())) {
                    last = System.currentTimeMillis();
                    return node;
                }
            } catch (Exception ignore) {
                logger.error(ignore.getMessage(), ignore);
            } finally {
                long t = System.currentTimeMillis() - begin;
                if (t > 50) {
                    String s = "Xsharded.ping2 cost:" + t;
                    if (!ThreadLocalLog.addLog(s)) {
                        LoggerFactory.getLogger("Slow").info(s);
                    }
                }
            }
        }
        long begin = System.currentTimeMillis();
        try {
            node.quit();
        } catch (Exception ignore) {
        }
        try {
            node.disconnect();
        } catch (Exception ignore) {
        }

        node = createResource();
        last = System.currentTimeMillis();

        String s = "Xsharded.ping2 create new connection cost:" + (last - begin);
        if (!ThreadLocalLog.addLog(s)) {
            LoggerFactory.getLogger("Slow").info(s);
        }
        return node;

    }

    public XedisConfig getConfig() {
        return config;
    }

    public Jedis getJedis(String key) {
        if (this.node == null) throw new IllegalStateException("oh no! no redis found!");
        this.node = ping(node);
        return this.node;
    }
}
