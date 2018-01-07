package com.Jonas.redis;

import redis.clients.jedis.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 客户端
 *
 * @author z
 */
public class Xedis extends XedisBasic {
    private String type;

    public Xedis(String type, XedisConfig config) {
        super(config);
        this.type = type;
    }

    public void disconnect() {
        super.disconnect();
    }

    String getType() {
        return type;
    }

    public Transaction multi() {
        Jedis j = getJedis(null);
        return j.multi();
    }

    public String set(String key, String value) {
        Jedis j = getJedis(key);
        return j.set(key, value);
    }

    public String get(String key) {
        Jedis j = getJedis(key);
        return j.get(key);
    }

    public String getSet(String key, String value) {
        Jedis j = getJedis(key);
        return j.getSet(key, value);
    }

    public List<String> mget(String... key) {
        Jedis j = getJedis(key[0]);
        return j.mget(key);
    }

    public Boolean exists(String key) {
        Jedis j = getJedis(key);
        return j.exists(key);
    }

    public String type(String key) {
        Jedis j = getJedis(key);
        return j.type(key);
    }

    public Long expire(String key, int seconds) {
        Jedis j = getJedis(key);
        return j.expire(key, seconds);
    }

    public Long decrBy(String key, long integer) {
        Jedis j = getJedis(key);
        return j.decrBy(key, integer);
    }

    public Long decr(String key) {
        Jedis j = getJedis(key);
        return j.decr(key);
    }

    public Long incr(String key) {
        Jedis j = getJedis(key);
        return j.incr(key);
    }

    public Long append(String key, String value) {
        Jedis j = getJedis(key);
        return j.append(key, value);
    }

    public Long hset(String key, String field, String value) {
        Jedis j = getJedis(key);
        return j.hset(key, field, value);
    }

    public String hget(String key, String field) {
        Jedis j = getJedis(key);
        return j.hget(key, field);
    }

    public String hmset(String key, Map<String, String> hash) {
        Jedis j = getJedis(key);
        return j.hmset(key, hash);
    }

    public List<String> hmget(String key, String... fields) {
        Jedis j = getJedis(key);
        return j.hmget(key, fields);
    }

    public Long hincrBy(String key, String field, long value) {
        Jedis j = getJedis(key);
        return j.hincrBy(key, field, value);
    }

    public Long del(String... key) {
        if (key == null || key.length == 0)
            return 0L;
        Jedis j = getJedis(key[0]);
        return j.del(key);
    }

    public Long hdel(String key, String... fields) {
        Jedis j = getJedis(key);
        return j.hdel(key, fields);
    }

    public Long hlen(String key) {
        Jedis j = getJedis(key);
        return j.hlen(key);
    }

    public Long rpush(String key, String... strings) {
        Jedis j = getJedis(key);
        return j.rpush(key, strings);
    }

    public Long llen(String key) {
        Jedis j = getJedis(key);
        return j.llen(key);
    }

    public String lpop(String key) {
        Jedis j = getJedis(key);
        return j.lpop(key);
    }

    public Long sadd(String key, String... members) {
        Jedis j = getJedis(key);
        return j.sadd(key, members);
    }

    public Set<String> smembers(String key) {
        Jedis j = getJedis(key);
        return j.smembers(key);
    }

    public Set<String> sinter(String... keys) {
        Jedis j = getJedis(keys[0]);
        return j.sinter(keys);
    }

    public Long srem(String key, String... members) {
        Jedis j = getJedis(key);
        return j.srem(key, members);
    }

    public Long scard(String key) {
        Jedis j = getJedis(key);
        return j.scard(key);
    }
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        Jedis j = getJedis(key);
        return j.sscan(key, cursor, params);
    }
    public Boolean sismember(String key, String member) {
        Jedis j = getJedis(key);
        return j.sismember(key, member);
    }

    public Long zadd(String key, double score, String member) {
        Jedis j = getJedis(key);
        return j.zadd(key, score, member);
    }

    public Long zadd(String key, Map<String, Double> scoreMembers) {
        Jedis j = getJedis(key);
        return j.zadd(key, scoreMembers);
    }

    public Double zincrby(String key, Double score, String member) {
        Jedis j = getJedis(key);
        return j.zincrby(key, score, member);
    }

    public Long zunionstore(String dstkey, ZParams params, String... sets) {
        Jedis j = getJedis(dstkey);
        return j.zunionstore(dstkey, params, sets);
    }

    public Set<String> zrange(String key, long start, long end) {
        Jedis j = getJedis(key);
        return j.zrange(key, start, end);
    }

    public Long zrem(String key, String... members) {
        Jedis j = getJedis(key);
        return j.zrem(key, members);
    }

    public Long zcard(String key) {
        Jedis j = getJedis(key);
        return j.zcard(key);
    }

    public Double zscore(String key, String member) {
        Jedis j = getJedis(key);
        return j.zscore(key, member);
    }

    public Long zcount(String key, String min, String max) {
        Jedis j = getJedis(key);
        return j.zcount(key, min, max);
    }

    public Set<String> zrangeByScore(String key, String min, String max) {
        Jedis j = getJedis(key);
        return j.zrangeByScore(key, min, max);
    }

    public Set<String> zrevrangeByScore(String key, String max, String min) {
        Jedis j = getJedis(key);
        return j.zrevrangeByScore(key, max, min);
    }

    public Set<String> zrangeByScore(String key, String min, String max,
                                     int offset, int count) {
        Jedis j = getJedis(key);
        return j.zrangeByScore(key, min, max, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max,
                                              int offset, int count) {
        Jedis j = getJedis(key);
        return j.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Set<String> zrevrangeByScore(String key, String max, String min,
                                        int offset, int count) {
        Jedis j = getJedis(key);
        return j.zrevrangeByScore(key, max, min, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min,
                                                 int offset, int count) {
        Jedis j = getJedis(key);
        return j.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    public Long zremrangeByScore(String key, String start, String end) {
        Jedis j = getJedis(key);
        return j.zremrangeByScore(key, start, end);
    }

    public String info() {
        Jedis j = getJedis(null);
        return j.info();
    }

    public String info(String section) {
        Jedis j = getJedis(null);
        return j.info(section);
    }

    public ScanResult<String> scan(String cursor, ScanParams scanParams) {
        Jedis j = getJedis(null);
        return j.scan(cursor, scanParams);
    }

    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams scanParams) {
        Jedis j = getJedis(key);
        return j.hscan(key, cursor, scanParams);
    }

    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        Jedis j = getJedis(key);
        return j.zscan(key, cursor, params);
    }

    public Long dbSize() {
        Jedis j = getJedis(null);
        return j.dbSize();
    }

    public long publish(String channel, String message) {
        Jedis j = getJedis(null);
        return j.publish(channel, message);
    }

    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        Jedis j = getJedis(null);
        j.subscribe(jedisPubSub, channels);
    }

    public void unsubscribe() {
        Jedis j = getJedis(null);
        try {
            Client client = j.getClient();
            if (client != null) {
                client.unsubscribe();
                client.flushAll();
            }
        } catch (Throwable ignore) {
        }
    }

    public Pipeline pipelined() {
        Jedis j = getJedis(null);
        return j.pipelined();
    }

    public Boolean setbit(String key,long offset, boolean value) {
        Jedis j = getJedis(null);
        return j.setbit(key, offset, value);
    }

    public Boolean getbit(String key, long offset) {
        Jedis j = getJedis(null);
        return j.getbit(key, offset);
    }

    public byte[] get(byte[] key) {
        Jedis j = getJedis(null);
        return j.get(key);
    }

    public long setnx(String key, String value) {
        Jedis j = getJedis(key);
        return j.setnx(key, value);
    }
    public String setex(String key, int seconds, String value) {
        Jedis j = getJedis(key);
        return j.setex(key, seconds, value);
    }


    public Long ttl(String key) {
        Jedis j = getJedis(key);
        return j.ttl(key);
    }
}
