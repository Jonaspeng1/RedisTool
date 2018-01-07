package com.Jonas.redis;

import java.util.List;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * 简单封装Pipeline，自动获取连接以及在sync中自动释放连接
 * Created by janon on 2015/6/29.
 */
public class WrapPipeline {
    private final String type;
    private Xedis xedis = null;
    private Pipeline pipeline = null;

    protected WrapPipeline(String type) {
        this.type = type;
    }

    private Xedis getClient() {
        if (xedis == null) xedis = XedisClient.getClient(type);
        return xedis;
    }

    private Pipeline getPipeline() {
        if (pipeline == null) pipeline = getClient().pipelined();
        return pipeline;
    }

    public void sync() {
        if (pipeline == null) return;
        try {
            pipeline.sync();
        } finally {
            XedisClient.returnClient(xedis);
            xedis = null;
            pipeline = null;
        }
    }

    public List<Object> syncAndReturnAll() {
        if (pipeline == null) return null;
        try {
            return pipeline.syncAndReturnAll();
        } finally {
            XedisClient.returnClient(xedis);
            xedis = null;
            pipeline = null;
        }
    }

    
    public Response<Long> del(String... keys) {
        return getPipeline().del(keys);
    }

    public Response<Long> zadd(String key, double score, String member) {
        return getPipeline().zadd(key, score, member);
    }

    public Response<Long> zrem(String key, String... member) {
        return getPipeline().zrem(key, member);
    }

    public Response<Long> expire(String key, int seconds) {
        return getPipeline().expire(key, seconds);
    }

    public Response<Long> srem(String key, String... member) {
        return getPipeline().srem(key, member);
    }

    public Response<Long> sadd(String key, String... member) {
        return getPipeline().sadd(key, member);
    }
    
    public Response<String> get(String key) {
        return getPipeline().get(key);
    }

    public Response<Double> zscore(String key, String member) {
        return getPipeline().zscore(key, member);
    }

    public Response<String> hget(String key, String field) {
        return getPipeline().hget(key, field);
    }

    public Response<String> setex(String key, int seconds, String value) {
        return getPipeline().setex(key, seconds, value);
    }
}
