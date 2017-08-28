package com.dempe.sample.log;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by dempezheng on 2017/8/28.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@Import(AccessLogConfiguration.class)
@Documented
public @interface EnableAccessLog {
    /**
     * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
     * to standard Java interface-based proxies. The default is {@code false}.
     *
     * @return whether to proxy or not to proxy the class
     */
    boolean proxyTargetClass() default false;
}
