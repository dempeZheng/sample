package com.dempe.sample.log;

import com.yy.ent.exception.logger.factory.FrequencyPeriodLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Dempe
 * Date: 2016/11/18
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class SampleLog {

    private final static Logger LOGGER = LoggerFactory.getLogger(SampleLog.class);

    private final static org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(SampleLog.class);

    public SampleLog() {
        FrequencyPeriodLoggerFactory factory = new FrequencyPeriodLoggerFactory();
        factory.init(300, 5, 10, 3);

    }

    public void logInfo(String msg) {
        LOGGER.info(msg);
        logger.info("log4j>>>>>>>:"+msg);
    }

    public void logErr(String msg) {
        LOGGER.error(msg);
        logger.error("log4j>>>>>>>:"+msg);
    }

    public static void main(String[] args) throws InterruptedException {

        SampleLog sampleLog = new SampleLog();
        sampleLog.logInfo(">>>>>>>>>>>>>>>>>>>>>>>>");
        TimeUnit.SECONDS.sleep(1L);

        sampleLog.logErr("-------------------");

    }


}
