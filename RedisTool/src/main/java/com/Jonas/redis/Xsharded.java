package com.Jonas.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Jonas.util.ThreadLocalLog;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class Xsharded {
    public static final int DEFAULT_WEIGHT = 1;
    private static final Logger logger = LoggerFactory.getLogger(Xsharded.class);
    private final static long pingBetweenEvictionRunsMillis = 10000L;
    private List<XshardInfo> shardInfos;
    private List<Jedis> nodes = new ArrayList<Jedis>();
    private Jedis node0;
    private long last = 0;
    private boolean needGetNew = false;

    public Xsharded(List<XshardInfo> shardinfos) {
        this.shardInfos = shardinfos;

        for (XshardInfo shardInfo : shardinfos) {
            Jedis node = shardInfo.createResource();
            if (node0 == null) {
                node0 = node;
            }

            nodes.add(node);
        }
    }

    public boolean isNeedGetNew() {
        return needGetNew;
    }

    public void setNeedGetNew(boolean needGetNew) {
        this.needGetNew = needGetNew;
    }

    public void disconnect() {
        for (Jedis jedis : nodes) {
            try {
                jedis.quit();
            } catch (Exception ignore) {
            }
            try {
                jedis.disconnect();
            } catch (Exception ignore) {
            }
        }
        this.shardInfos = null;
        this.nodes = null;
        this.node0 = null;
    }

    public boolean ping() {
        if (System.currentTimeMillis() - last < pingBetweenEvictionRunsMillis) {
            last = System.currentTimeMillis();
            return true;
        }
        long begin = System.currentTimeMillis();
        try {
            for (Jedis shard : nodes) {
                if (!shard.isConnected() || !"PONG".equals(shard.ping())) {
                    return false;
                }
            }
            last = System.currentTimeMillis();
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        } finally {
            long t = System.currentTimeMillis() - begin;
            if (t > 50) {
                String s = "Xsharded.ping1(cost:" + t + ")";
                if (!ThreadLocalLog.addLog(s)) {
                    LoggerFactory.getLogger("Slow").info(s);
                }
            }
        }
    }

    Jedis ping(Jedis node, XshardInfo info) {
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
//                    System.out.println("ping " + last);
                    last = System.currentTimeMillis();
                    return node;
                }
            } catch (Exception ignore) {
                logger.error(ignore.getMessage(), ignore);
            } finally {
                long t = System.currentTimeMillis() - begin;
                if (t > 50) {
                    String s = "Xsharded.ping2(cost:" + t + ")";
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

        node = info.createResource();
        last = System.currentTimeMillis();

        String s = "Xsharded.ping2(create new connection cost:" + (last - begin) + ")";
        if (!ThreadLocalLog.addLog(s)) {
            LoggerFactory.getLogger("Slow").info(s);
        }
        return node;

    }

    /**
     * 根据key分区取数据客户端,取key前5字符int作分区依据
     *
     * @param key
     * @return
     */
    public Jedis getShard(String key) {
        if (nodes == null || nodes.size() == 0) throw new IllegalStateException("oh no! no redis found!");
        if (nodes.size() == 1) {
            node0 = ping(node0, shardInfos.get(0));
            nodes.set(0, node0);
            return node0;
        }
        int ik = partedkey(key), i = 0;
        for (XshardInfo info : shardInfos) {
            if (info.minkey <= ik && info.maxkey >= ik) {
                Jedis node = ping(nodes.get(i), info);
                nodes.set(i, node);
                return node;
            }
            i++;
        }
        return node0;
    }

    public int partedkey(String key) {
        if (key == null || key.length() == 0) return 0;
        try {
            int i = key.indexOf(":");
            if (i <= 0)
                return 0;
            return Integer.parseInt(key.substring(0, i));
        } catch (StringIndexOutOfBoundsException e1) {
            return 0;
        } catch (NumberFormatException e2) {
            return 0;
        }
    }
}
