package com.Jonas.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.util.ShardInfo;
import redis.clients.util.Sharded;

/**
 * Redis Server 配置
 * 分区需要设置 minkey,maxkey
 *
 * @author z
 */
public class XshardInfo extends ShardInfo<Jedis> {

    public static int MAXKEY = 99999;
    public int timeout;
    public String host;
    public int port;
    public String password = null;
    public String name = null;
    public int minkey = 0;
    public int maxkey = MAXKEY;

    public XshardInfo(String host) {
        this(host, Protocol.DEFAULT_PORT);
    }


    public XshardInfo(String host, String name) {
        this(host, Protocol.DEFAULT_PORT, name);
    }

    public XshardInfo(String host, int port) {
        this(host, port, 2000);
    }

    public XshardInfo(String host, int port, String name) {
        this(host, port, 2000, name);
    }

    public XshardInfo(String host, int port, int timeout) {
        this(host, port, timeout, Sharded.DEFAULT_WEIGHT);
    }

    public XshardInfo(String host, int port, int timeout, String name) {
        this(host, port, timeout, Sharded.DEFAULT_WEIGHT);
        this.name = name;
    }

    public XshardInfo(String host, int port, int timeout, int weight) {
        super(weight);
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public String toString() {
        return host + ":" + port + "*" + getWeight();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public Jedis createResource() {
        Jedis jedis = new Jedis(host, port, timeout);
        if (password != null && password.length() > 0)
            jedis.auth(password);
        return jedis;
    }

    @Override
    public String getName() {
        return name;
    }
}
