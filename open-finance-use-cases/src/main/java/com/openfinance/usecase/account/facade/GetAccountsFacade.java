package com.openfinance.usecase.account.facade;

import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.usecase.IUseCase;
import com.openfinance.usecase.account.input.GetAccountsInput;
import com.openfinance.usecase.account.output.GetAccountsOutput;
import com.openfinance.usecase.account.service.AccountAccessLoggingService;
import com.openfinance.usecase.account.service.AccountsBusinessService;
import com.openfinance.usecase.account.service.GetAccountsUseCaseImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Facade for Get Accounts operations
 * Provides a simplified interface and handles cross-cutting concerns like logging and metrics
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetAccountsFacade  {

    private final GetAccountsUseCaseImpl getAccountsUseCase;
    private final AccountsBusinessService accountsBusinessService;
    private final AccountAccessLoggingService loggingService;

    private static final long SLA_THRESHOLD_MS = 1500; // High frequency endpoint SLA

    /**
     * Executes the Get Accounts operation with full monitoring and logging
     */
    public GetAccountsOutput getAccounts(GetAccountsInput input) {
        long startTime = System.currentTimeMillis();

        try {
            // Log operation start
            loggingService.logAccountAccessStart(input);

            // Validate input parameters
            long validationStartTime = System.currentTimeMillis();
            accountsBusinessService.validateGetAccountsInput(input);
            long validationTime = System.currentTimeMillis() - validationStartTime;

            // Execute use case
            long useCaseStartTime = System.currentTimeMillis();
            GetAccountsOutput result = getAccountsUseCase.execute(input);
            long useCaseTime = System.currentTimeMillis() - useCaseStartTime;

            // Calculate total execution time
            long totalExecutionTime = System.currentTimeMillis() - startTime;

            // Log success and performance metrics
            loggingService.logAccountAccessSuccess(input, result.getAccountCount(), totalExecutionTime);
            loggingService.logPerformanceMetrics(
                    "GET_ACCOUNTS",
                    totalExecutionTime,
                    validationTime,
                    useCaseTime - validationTime, // External call time approximation
                    validationTime
            );

            // Log compliance metrics
            boolean withinSLA = totalExecutionTime <= SLA_THRESHOLD_MS;
            loggingService.logComplianceMetrics(
                    "/accounts",
                    withinSLA,
                    totalExecutionTime,
                    input.organizationId(),
                    result.getAccountCount()
            );

            if (!withinSLA) {
                log.warn("SLA threshold exceeded for GET_ACCOUNTS operation. Expected: {}ms, Actual: {}ms",
                        SLA_THRESHOLD_MS, totalExecutionTime);
            }

            return result;

        } catch (BusinessRuleViolationException e) {
            long totalExecutionTime = System.currentTimeMillis() - startTime;
            loggingService.logAccountAccessFailure(input, e.getErrorCode(), e.getMessage(), totalExecutionTime);
            throw e;

        } catch (Exception e) {
            long totalExecutionTime = System.currentTimeMillis() - startTime;
            loggingService.logAccountAccessFailure(input, "INTERNAL_ERROR", e.getMessage(), totalExecutionTime);

            log.error("Unexpected error in GetAccountsFacade for consentId: {}", input.consentId(), e);
            throw new BusinessRuleViolationException("Internal error processing accounts request", e);
        }
    }

    /**
     * Validates input and returns validation summary
     */
    public InputValidationSummary validateInput(GetAccountsInput input) {
        try {
            accountsBusinessService.validateGetAccountsInput(input);
            return InputValidationSummary.builder()
                    .valid(true)
                    .effectivePageSize(accountsBusinessService.calculateEffectivePageSize(input))
                    .validationMessage("Input validation passed")
                    .build();

        } catch (BusinessRuleViolationException e) {
            return InputValidationSummary.builder()
                    .valid(false)
                    .effectivePageSize(input.pageSize())
                    .validationMessage(e.getMessage())
                    .errorCode(e.getErrorCode())
                    .build();
        }
    }

    /**
     * Gets operation health status
     */
    public OperationHealthStatus getHealthStatus() {
        return OperationHealthStatus.builder()
                .operationName("GET_ACCOUNTS")
                .endpoint("/accounts")
                .slaThresholdMs(SLA_THRESHOLD_MS)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Record for input validation summary
     */
    @lombok.Builder
    public record InputValidationSummary(
            boolean valid,
            int effectivePageSize,
            String validationMessage,
            String errorCode
    ) {}

    /**
     * Record for operation health status
     */
    @lombok.Builder
    public record OperationHealthStatus(
            String operationName,
            String endpoint,
            long slaThresholdMs,
            LocalDateTime timestamp
    ) {}
}
