package com.dempe.sample.log.v2;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by dempezheng on 2017/8/28.
 */
@Aspect
public class LogAccessAspect {
    private final static Logger logger = LoggerFactory.getLogger(LogAccessAspect.class);

    @Pointcut("@annotation(com.dempe.sample.log.AccessLog)")
    public void logAccessPointcut() {
    }

    @Around("logAccessPointcut()")
    public Object methodsAnnotatedWithCacheRemove(final ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String methodName = signature.getMethod().getName();
        long beginTime = System.currentTimeMillis();
        logger.info("req:methodName:{}, args:{}", methodName, Arrays.toString(pjp.getArgs()));
        Object proceed = pjp.proceed();
        logger.info("rsp:methodName:{}, result:{}, takeTime:{}", methodName, proceed, System.currentTimeMillis() - beginTime);
        return proceed;
    }

}
