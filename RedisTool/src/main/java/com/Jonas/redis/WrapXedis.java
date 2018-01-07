package com.Jonas.redis;

import redis.clients.jedis.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Janon
 * Date: 14-1-20 下午4:35
 */
public interface WrapXedis {

    String set(String key, String value);

    String get(String key);
    
    String getQuiet(String key);

    String getSet(String key, String value);

    List<String> mget(String... key);
    
    List<String> mgetQuiet(String... key);

    Boolean exists(String key);

    Long decr(String key);

    Long decrBy(String key, long integer);

    Long incr(String key);

    Long hsetQuiet(String key, String field, String value);

    Long hset(String key, String field, String value);

    String hmsetQuiet(String key, Map<String, String> hash);

    String hmset(String key, Map<String, String> hash);

    String hgetQuiet(String key, String field);

    String hget(String key, String field);

    List<String> hmgetQuiet(String key, String... fields);

    List<String> hmget(String key, String... fields);
    
    List<String> hvals(String key, String... fields);

    Long hincrBy(String key, String field, long value);

    Long del(String... key);

    Long hdelQuiet(String key, String... fields);

    Long hdel(String key, String... fields);

    Long hlenQuiet(String key);

    Long hlen(String key);

    Long sadd(String key, String... members);

    Long scard(String key);
    Long delQuiet(String... key);
    Set<String> smembers(String key);

    Set<String> sinter(String... keys);

    Long srem(String key, String... members);

    Boolean sismember(String key, String member);
    
    ScanResult<String> sscan(String key, String cursor, ScanParams params);


    Long zadd(String key, double score, String member);
    

    Long zadd(String key, Map<String, Double> scoreMembers);

    Double zincrby(String key, Double score, String member);

    Long zunionstore(String dstkey, ZParams params, String... sets);

    Set<String> zrange(String key, long start, long end);

    Long zrem(String key, String... members);

    Long zcard(String key);

    Double zscore(String key, String member);

    Long zcount(String key, String min, String max);

    Set<String> zrangeByScore(String key, String min, String max);

    Set<String> zrevrangeByScore(String key, String max, String min);

    Set<String> zrangeByScore(String key, String min, String max,
                              int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max,
                                       int offset, int count);

    Set<String> zrevrangeByScore(String key, String max, String min,
                                 int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min,
                                          int offset, int count);

    Long zremrangeByScore(String key, String start, String end);

    Long expire(String key, int seconds);

    Long rpush(String key, String... strings);

    String lpop(String key);

    Long llen(String key);

    String info();

    String info(String section);

    ScanResult<String> scan(String cursor, ScanParams scanParams);

    ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams scanParams);

    ScanResult<Tuple> zscan(String key, String cursor, ScanParams params);

    Long dbSize();

    long publish(String channel, String msg);

    void subscribe(JedisPubSub jedisPubSub, String... channels);

    WrapPipeline pipelined();

    Boolean setbit(String key, long offset, boolean value);

    Boolean getbit(String key, long offset);

    byte[] get(byte[] key);

    long setnx(String key, String value);
    
    String getXedisInfo();
    
    Long setnxQuiet(String key, String value);

    String setexQuiet(String key, int seconds, String value);

    Set<String> keys(String pattern);

    public Long ttl(String key);
    
    Long zremrangeByRank(String key, long start, long end);


}
