package com.Jonas.redis;

import redis.clients.jedis.JedisPubSub;

/**
 * JedisPubSubAdapter
 * Created by janon on 2015/5/19.
 */
public class JedisPubSubAdapter extends JedisPubSub {
    @Override
    public void onMessage(String channel, String message) {
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
    }
}