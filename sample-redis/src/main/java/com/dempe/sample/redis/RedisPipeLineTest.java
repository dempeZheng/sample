package com.dempe.sample.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * Created with IntelliJ IDEA.
 * User: Dempe
 * Date: 2016/11/7
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class RedisPipeLineTest {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("");
        Pipeline p1 = jedis.pipelined();
        p1.incr("");
        p1.incr("");
        p1.sync();
    }
}
