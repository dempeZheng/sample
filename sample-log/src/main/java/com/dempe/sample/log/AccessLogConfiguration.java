package com.dempe.sample.log;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dempezheng on 2017/8/28.
 */
@Configuration
public class AccessLogConfiguration extends AbstractPointcutAdvisor implements IntroductionAdvisor {
    private Advice advice;
    private Pointcut pointcut;

    @PostConstruct
    public void init() {
        Set<Class<? extends Annotation>> retryableAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>(1);
        retryableAnnotationTypes.add(AccessLog.class);
        this.pointcut = buildPointcut(retryableAnnotationTypes);
        this.advice = buildAdvice();

    }

    @Override
    public ClassFilter getClassFilter() {
        return pointcut.getClassFilter();
    }

    @Override
    public Class<?>[] getInterfaces() {
        return new Class[]{com.dempe.sample.log.AccessLog.class};
    }

    @Override
    public void validateInterfaces() throws IllegalArgumentException {
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    protected Advice buildAdvice() {
        AccessLogInterceptor interceptor = new AccessLogInterceptor();
        return interceptor;
    }

    /**
     * Calculate a pointcut for the given retry annotation types, if any.
     *
     * @param retryAnnotationTypes the retry annotation types to introspect
     * @return the applicable Pointcut object, or {@code null} if none
     */
    protected Pointcut buildPointcut(Set<Class<? extends Annotation>> retryAnnotationTypes) {
        ComposablePointcut result = null;
        for (Class<? extends Annotation> retryAnnotationType : retryAnnotationTypes) {
            Pointcut filter = new AnnotationClassOrMethodPointcut(retryAnnotationType);
            if (result == null) {
                result = new ComposablePointcut(filter);
            } else {
                result.union(filter);
            }
        }
        return result;
    }

    private final class AnnotationClassOrMethodPointcut extends StaticMethodMatcherPointcut {

        private final MethodMatcher methodResolver;

        AnnotationClassOrMethodPointcut(Class<? extends Annotation> annotationType) {
            this.methodResolver = new AnnotationMethodMatcher(annotationType);
            setClassFilter(new AnnotationClassOrMethodFilter(annotationType));
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return getClassFilter().matches(targetClass) || this.methodResolver.matches(method, targetClass);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AnnotationClassOrMethodPointcut)) {
                return false;
            }
            AnnotationClassOrMethodPointcut otherAdvisor = (AnnotationClassOrMethodPointcut) other;
            return ObjectUtils.nullSafeEquals(this.methodResolver, otherAdvisor.methodResolver);
        }

    }

    private final class AnnotationClassOrMethodFilter extends AnnotationClassFilter {

        private final AnnotationMethodsResolver methodResolver;

        AnnotationClassOrMethodFilter(Class<? extends Annotation> annotationType) {
            super(annotationType, true);
            this.methodResolver = new AnnotationMethodsResolver(annotationType);
        }

        @Override
        public boolean matches(Class<?> clazz) {
            return super.matches(clazz) || this.methodResolver.hasAnnotatedMethods(clazz);
        }

    }

    private static class AnnotationMethodsResolver {

        private Class<? extends Annotation> annotationType;

        public AnnotationMethodsResolver(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        public boolean hasAnnotatedMethods(Class<?> clazz) {
            final AtomicBoolean found = new AtomicBoolean(false);
            ReflectionUtils.doWithMethods(clazz,
                    new ReflectionUtils.MethodCallback() {
                        @Override
                        public void doWith(Method method) throws IllegalArgumentException,
                                IllegalAccessException {
                            if (found.get()) {
                                return;
                            }
                            Annotation annotation = AnnotationUtils.findAnnotation(method,
                                    annotationType);
                            if (annotation != null) {
                                found.set(true);
                            }
                        }
                    });
            return found.get();
        }

    }
}
