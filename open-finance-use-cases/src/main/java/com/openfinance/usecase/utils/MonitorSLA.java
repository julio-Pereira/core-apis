package com.openfinance.usecase.utils;

import com.openfinance.usecase.account.service.RateLimitValidationService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic SLA compliance monitoring
 * Monitors execution time against SLA thresholds for Open Finance compliance
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorSLA {

    /**
     * SLA threshold in milliseconds
     * Default values based on Open Finance frequency categories:
     * - High Frequency: 1500ms
     * - Medium-High: 1500ms
     * - Medium: 2000ms
     * - Low: 4000ms
     */
    long thresholdMs() default 1500;

    /**
     * Endpoint path for compliance reporting
     */
    String endpoint() default "";

    /**
     * Frequency category for SLA classification
     */
    FrequencyCategory category() default FrequencyCategory.HIGH;

}