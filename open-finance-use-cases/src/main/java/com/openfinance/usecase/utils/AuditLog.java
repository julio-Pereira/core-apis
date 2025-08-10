package com.openfinance.usecase.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic audit logging
 * Logs method entry, exit, parameters, and results for compliance
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * Whether to log method parameters (be careful with sensitive data)
     */
    boolean logParameters() default false;

    /**
     * Whether to log return values
     */
    boolean logReturnValue() default true;

    /**
     * Operation type for audit classification
     */
    String operationType() default "";

    /**
     * Whether this operation accesses sensitive data
     */
    boolean sensitiveData() default true;
}