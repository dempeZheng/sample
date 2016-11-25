package com.dempe.sample.thrift;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Dempe
 * Date: 2016/11/25
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
public class SampleThriftServer extends AbstractThriftServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleThriftServer.class);
    private static AtomicInteger rejected = new AtomicInteger(0);
    private static RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOGGER.info("SampleThriftServer reject: " + r + ", count: " + rejected.incrementAndGet());
        }
    };

    private ExecutorService es = new ThreadPoolExecutor(10, 2000, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            new ThreadFactoryBuilder().setNameFormat("lbs worker : %d").build(), handler);

    @Value("${thrift.port}")
    private Integer port;
    @Value("${thrift.framed.size:16777216}")
    private Integer framedSize;



    @PostConstruct
    public void start() {
        super.start();
    }

    @Override
    @PreDestroy
    public void close() {
        super.close();
        es.shutdown();
        try {
            if (!es.awaitTermination(30, TimeUnit.SECONDS)) {
                es.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for SelectorServer ExecutorService to be shutdown.");
        }
    }

    @Override
    protected TProcessor getProcessor() {
         //new Processor(new Handler(){
        //  // service();
        // })
        return null;
    }

    @Override
    public String getServerName() {
        return "Sample";
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected int getFramedSize() {
        return framedSize;
    }
}
