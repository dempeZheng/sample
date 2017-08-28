package com.dempe.sample.log;

/**
 * Created by dempezheng on 2017/8/28.
 */
public class SampleLogService {

    @AccessLog
    public String hello(String msg) {
        return "hello " + msg;
    }
}
