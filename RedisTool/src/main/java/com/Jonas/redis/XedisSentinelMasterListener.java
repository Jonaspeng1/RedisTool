package com.Jonas.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * XedisSentinelMasterListener
 * Created by janon on 2015/5/19.
 */
class XedisSentinelMasterListener extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(XedisSentinelMasterListener.class);
    private String type;
    private String masterName;
    private String host;
    private int port;
    private long subscribeRetryWaitTimeMillis = 5000;
    protected Jedis j;
    protected AtomicBoolean running = new AtomicBoolean(false);

    public XedisSentinelMasterListener(String type, String masterName, String host, int port) {
        this.type = type;
        this.masterName = masterName;
        this.host = host;
        this.port = port;
        setName("XedisSentinelMasterListener-" + host + ":" + port + "-" + masterName);
        setDaemon(true);
    }

    public void run() {
        running.set(true);
        while (running.get()) {
            j = new Jedis(host, port);
            try {
                j.subscribe(new JedisPubSubAdapter() {
                    @Override
                    public void onMessage(String channel, String message) {
                        handleMessage(channel, message);
                    }
                }, "+switch-master");
            } catch (JedisConnectionException e) {
                close(j);
                if (running.get()) {
                    logger.warn("Lost connection to Sentinel at {}:{}. Sleeping {}ms and retrying.", host, port, subscribeRetryWaitTimeMillis);
                    try {
                        Thread.sleep(subscribeRetryWaitTimeMillis);
                    } catch (Throwable ignore) {
                    }
                } else {
                    logger.info("Unsubscribing from Sentinel at {}:{}", host, port);
                }
            }
        }
    }

    public void handleMessage(@SuppressWarnings("UnusedParameters") String channel, String message) {
        logger.info("Sentinel {}:{} published:{}.", host, port, message);
        String[] switchMasterMsg = message.split(" ");
        if (switchMasterMsg.length > 3) {
            if (masterName.equals(switchMasterMsg[0])) {
                String host = switchMasterMsg[3];
                int port = Integer.parseInt(switchMasterMsg[4]);
                XedisClient.updateClient(type, host, port);
//                initPool(toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4])));
            } else {
                logger.info("Ignoring message on +switch-master for master name {}, our master name is {}"
                        , switchMasterMsg[0], masterName);
            }
        } else {
            logger.warn("Invalid message received on Sentinel {}:{} on channel +switch-master:{}", host, port, message);
        }
    }

    public void shutdown() {
        try {
            logger.warn("Shutting down listener on {}:{}", host, port);
            running.set(false);
            // This isn't good, the Jedis object is not thread safe
            close(j);
        } catch (Exception e) {
            logger.warn("Caught exception while shutting down:{}", e.getMessage());
        }
    }

    private void close(Jedis j) {
        try {
            j.quit();
        } catch (Exception ignore) {
        }
        try {
            j.disconnect();
        } catch (Exception ignore) {
        }
    }
}
