package com.openfinance.usecase.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic performance monitoring
 * When applied to a method, automatically logs execution time and performance metrics
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorPerformance {

    /**
     * Operation name for logging (defaults to method name)
     */
    String operationName() default "";

    /**
     * Whether to log detailed breakdown of execution phases
     */
    boolean detailedLogging() default true;

    /**
     * Threshold in milliseconds for warning logs
     */
    long warningThresholdMs() default 1000;
}