package com.openfinance.usecase.account.facade;


import com.openfinance.usecase.IUseCase;
import com.openfinance.usecase.account.input.GetAccountsInput;
import com.openfinance.usecase.account.output.GetAccountsOutput;
import com.openfinance.usecase.account.service.AccountsBusinessService;
import com.openfinance.usecase.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Simplified Facade using AOP for cross-cutting concerns
 * All logging, metrics, and monitoring are handled automatically by aspects
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetAccountsFacade {

    private final IUseCase<GetAccountsInput, GetAccountsOutput> getAccountsUseCase;
    private final AccountsBusinessService accountsBusinessService;

    /**
     * Executes the Get Accounts operation
     * Cross-cutting concerns (logging, metrics, SLA monitoring) handled automatically by AOP
     */
    @AuditLog(operationType = "GET_ACCOUNTS", sensitiveData = true)
    @MonitorPerformance(operationName = "GET_ACCOUNTS", warningThresholdMs = 1000)
    @MonitorSLA(thresholdMs = 1500, endpoint = "/accounts", category = FrequencyCategory.HIGH)
    @CollectMetrics(metricPrefix = "open_finance.accounts.get",
            tags = {"endpoint=/accounts", "frequency=high"})
    public GetAccountsOutput getAccounts(GetAccountsInput input) {
        // Validate input parameters
        accountsBusinessService.validateGetAccountsInput(input);

        // Execute use case - all monitoring handled by aspects
        return getAccountsUseCase.execute(input);
    }

    /**
     * Validates input and returns validation summary
     * No cross-cutting concerns needed here, so no annotations
     */
    public InputValidationSummary validateInput(GetAccountsInput input) {
        try {
            accountsBusinessService.validateGetAccountsInput(input);
            return InputValidationSummary.builder()
                    .valid(true)
                    .effectivePageSize(accountsBusinessService.calculateEffectivePageSize(input))
                    .validationMessage("Input validation passed")
                    .build();

        } catch (Exception e) {
            return InputValidationSummary.builder()
                    .valid(false)
                    .effectivePageSize(input.pageSize())
                    .validationMessage(e.getMessage())
                    .errorCode(extractErrorCode(e))
                    .build();
        }
    }

    /**
     * Gets operation health status
     */
    @MonitorPerformance(operationName = "GET_HEALTH_STATUS", warningThresholdMs = 100)
    public OperationHealthStatus getHealthStatus() {
        return OperationHealthStatus.builder()
                .operationName("GET_ACCOUNTS")
                .endpoint("/accounts")
                .slaThresholdMs(1500L)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Batch validation for multiple inputs (demonstrating AOP scalability)
     */
    @MonitorPerformance(operationName = "BATCH_VALIDATE", warningThresholdMs = 500)
    @CollectMetrics(metricPrefix = "open_finance.accounts.batch_validate")
    public BatchValidationResult validateBatch(java.util.List<GetAccountsInput> inputs) {
        java.util.List<InputValidationSummary> results = inputs.stream()
                .map(this::validateInput)
                .toList();

        long validCount = results.stream().mapToLong(r -> r.valid() ? 1 : 0).sum();

        return BatchValidationResult.builder()
                .totalInputs(inputs.size())
                .validInputs((int) validCount)
                .invalidInputs(inputs.size() - (int) validCount)
                .validationResults(results)
                .build();
    }

    // Helper method
    private String extractErrorCode(Exception e) {
        if (e instanceof com.openfinance.core.exceptions.BusinessRuleViolationException) {
            return ((com.openfinance.core.exceptions.BusinessRuleViolationException) e).getErrorCode();
        }
        return "VALIDATION_ERROR";
    }

    // Records for return types
    @lombok.Builder
    public record InputValidationSummary(
            boolean valid,
            int effectivePageSize,
            String validationMessage,
            String errorCode
    ) {}

    @lombok.Builder
    public record OperationHealthStatus(
            String operationName,
            String endpoint,
            long slaThresholdMs,
            LocalDateTime timestamp
    ) {}

    @lombok.Builder
    public record BatchValidationResult(
            int totalInputs,
            int validInputs,
            int invalidInputs,
            java.util.List<InputValidationSummary> validationResults
    ) {}
}