package com.dempe.sample.log;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by dempezheng on 2017/8/28.
 */
@Configuration
@EnableAccessLog(proxyTargetClass = true)
public class Application {

    @Bean
    public SampleLogService service() {
        return new SampleLogService();
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                Application.class);
        SampleLogService service = context.getBean(SampleLogService.class);
        String log = service.hello("log");
        System.out.println(log);

    }


}


