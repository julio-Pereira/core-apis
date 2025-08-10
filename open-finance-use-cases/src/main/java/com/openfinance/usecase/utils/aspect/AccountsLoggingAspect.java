package com.openfinance.usecase.utils.aspect;

import com.openfinance.usecase.account.input.GetAccountsInput;
import com.openfinance.usecase.account.output.GetAccountsOutput;
import com.openfinance.usecase.account.service.AccountAccessLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for automatic logging of account operations
 * Implements cross-cutting concern for audit and compliance logging
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AccountsLoggingAspect {

    private final AccountAccessLoggingService loggingService;

    /**
     * Around advice for GetAccountsFacade.getAccounts method
     * Automatically logs start, success, failure, and performance metrics
     */
    @Around("execution(* com.openfinance.usecase.account.facade.GetAccountsFacade.getAccounts(..))")
    public Object logAccountAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        GetAccountsInput input = (GetAccountsInput) joinPoint.getArgs()[0];

        // Log operation start
        loggingService.logAccountAccessStart(input);

        try {
            // Execute the actual method
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            GetAccountsOutput output = (GetAccountsOutput) result;

            // Log successful completion
            loggingService.logAccountAccessSuccess(input, output.getAccountCount(), executionTime);

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            String errorCode = extractErrorCode(e);

            // Log failure
            loggingService.logAccountAccessFailure(input, errorCode, e.getMessage(), executionTime);

            throw e;
        }
    }

    /**
     * Around advice for performance monitoring
     */
    @Around("@annotation(com.openfinance.usecase.account.annotation.MonitorPerformance)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String operationName = joinPoint.getSignature().getName().toUpperCase();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            // Log performance metrics
            loggingService.logPerformanceMetrics(
                    operationName,
                    executionTime,
                    0, // validation time would need separate measurement
                    executionTime, // total execution time
                    0  // processing time would need separate measurement
            );

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.warn("Performance monitoring - Operation: {}, ExecutionTime: {}ms, Status: FAILED",
                    operationName, executionTime);
            throw e;
        }
    }

    /**
     * Around advice for SLA compliance monitoring
     */
    @Around("@annotation(com.openfinance.usecase.account.annotation.MonitorSLA)")
    public Object monitorSLA(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Extract SLA threshold from annotation or use default
        long slaThreshold = 1500; // Default for high frequency endpoints

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            boolean withinSLA = executionTime <= slaThreshold;

            // Extract input to get organization details
            if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof GetAccountsInput) {
                GetAccountsInput input = (GetAccountsInput) joinPoint.getArgs()[0];
                GetAccountsOutput output = (GetAccountsOutput) result;

                loggingService.logComplianceMetrics(
                        "/accounts",
                        withinSLA,
                        executionTime,
                        input.organizationId(),
                        output.getAccountCount()
                );

                if (!withinSLA) {
                    log.warn("SLA violation detected - Expected: {}ms, Actual: {}ms, OrganizationId: {}",
                            slaThreshold, executionTime, input.organizationId());
                }
            }

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("SLA monitoring failed - ExecutionTime: {}ms, Error: {}", executionTime, e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts error code from exception
     */
    private String extractErrorCode(Exception e) {
        if (e instanceof com.openfinance.core.exceptions.BusinessRuleViolationException) {
            return ((com.openfinance.core.exceptions.BusinessRuleViolationException) e).getErrorCode();
        }
        return "INTERNAL_ERROR";
    }
}