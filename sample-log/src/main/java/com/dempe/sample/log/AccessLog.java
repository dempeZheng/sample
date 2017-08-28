package com.dempe.sample.log;

import java.lang.annotation.*;

/**
 * Created by dempezheng on 2017/8/28.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLog {
}
