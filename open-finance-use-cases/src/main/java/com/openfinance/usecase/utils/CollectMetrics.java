package com.openfinance.usecase.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic metrics collection
 * Collects and publishes metrics for monitoring and alerting
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CollectMetrics {

    /**
     * Metric name prefix
     */
    String metricPrefix() default "";

    /**
     * Additional tags for metrics
     */
    String[] tags() default {};

    /**
     * Whether to count successful executions
     */
    boolean countSuccess() default true;

    /**
     * Whether to count failed executions
     */
    boolean countErrors() default true;

    /**
     * Whether to measure execution time
     */
    boolean measureTime() default true;
}
