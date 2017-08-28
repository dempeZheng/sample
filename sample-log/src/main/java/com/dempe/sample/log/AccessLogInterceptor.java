package com.dempe.sample.log;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.IntroductionInterceptor;

import java.util.Arrays;

/**
 * Created by dempezheng on 2017/8/28.
 */
public class AccessLogInterceptor implements IntroductionInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(AccessLogInterceptor.class);

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String methodName = methodInvocation.getMethod().getName();
        long beginTime = System.currentTimeMillis();
        logger.info("req:methodName:{}, args:{}", methodName, Arrays.toString(methodInvocation.getArguments()));
        Object proceed = methodInvocation.proceed();
        logger.info("rsp:methodName:{}, result:{}, takeTime:{}", methodName, proceed, System.currentTimeMillis() - beginTime);
        return proceed;
    }

    @Override
    public boolean implementsInterface(Class<?> aClass) {
        return AccessLog.class.isAssignableFrom(aClass);
    }

}
