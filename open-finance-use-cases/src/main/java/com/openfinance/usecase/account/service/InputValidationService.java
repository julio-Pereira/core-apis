package com.openfinance.usecase.account.service;

import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.usecase.account.input.GetAccountsInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for comprehensive input validation following Open Finance standards
 * Provides detailed validation with specific error messages and codes
 */
@Slf4j
@Service
public class InputValidationService {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private static final Pattern FAPI_AUTH_DATE_PATTERN = Pattern.compile(
            "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \\d{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{4} \\d{2}:\\d{2}:\\d{2} (GMT|UTC)$"
    );

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    /**
     * Performs comprehensive validation of GetAccountsInput
     * Returns detailed validation result with all errors found
     */
    public ValidationResult validateGetAccountsInput(GetAccountsInput input) {
        log.debug("Starting comprehensive validation for GetAccountsInput");

        List<ValidationError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate required fields
        validateRequiredFields(input, errors);

        // Validate pagination parameters
        validatePaginationParameters(input, errors, warnings);

        // Validate FAPI headers
        validateFapiHeaders(input, errors, warnings);

        // Validate optional parameters
        validateOptionalParameters(input, warnings);

        ValidationResult result = ValidationResult.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .inputSummary(createInputSummary(input))
                .build();

        if (result.valid) {
            log.debug("Input validation passed with {} warnings", warnings.size());
        } else {
            log.warn("Input validation failed with {} errors and {} warnings", errors.size(), warnings.size());
        }

        return result;
    }

    /**
     * Validates required fields
     */
    private void validateRequiredFields(GetAccountsInput input, List<ValidationError> errors) {
        // Consent ID validation
        if (input.consentId() == null || input.consentId().trim().isEmpty()) {
            errors.add(ValidationError.builder()
                    .field("consentId")
                    .code("FIELD_REQUIRED")
                    .message("Consent ID is required and cannot be empty")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        } else if (input.consentId().length() > 100) {
            errors.add(ValidationError.builder()
                    .field("consentId")
                    .code("FIELD_TOO_LONG")
                    .message("Consent ID cannot exceed 100 characters")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        }

        // Organization ID validation
        if (input.organizationId() == null || input.organizationId().trim().isEmpty()) {
            errors.add(ValidationError.builder()
                    .field("organizationId")
                    .code("FIELD_REQUIRED")
                    .message("Organization ID is required and cannot be empty")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        }

        // X-FAPI-Interaction-ID validation (most critical)
        if (input.xFapiInteractionId() == null || input.xFapiInteractionId().trim().isEmpty()) {
            errors.add(ValidationError.builder()
                    .field("xFapiInteractionId")
                    .code("FAPI_HEADER_REQUIRED")
                    .message("X-FAPI-Interaction-ID header is mandatory")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        } else if (!UUID_PATTERN.matcher(input.xFapiInteractionId()).matches()) {
            errors.add(ValidationError.builder()
                    .field("xFapiInteractionId")
                    .code("FAPI_HEADER_INVALID_FORMAT")
                    .message("X-FAPI-Interaction-ID must be a valid UUID format")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        }
    }

    /**
     * Validates pagination parameters according to Open Finance rules
     */
    private void validatePaginationParameters(GetAccountsInput input, List<ValidationError> errors, List<String> warnings) {
        // Page validation
        if (input.page() == null) {
            errors.add(ValidationError.builder()
                    .field("page")
                    .code("FIELD_REQUIRED")
                    .message("Page number is required")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        } else if (input.page() < 1) {
            errors.add(ValidationError.builder()
                    .field("page")
                    .code("PAGE_INVALID_RANGE")
                    .message("Page number must be greater than or equal to 1")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        } else if (input.page() > 2147483647) {
            errors.add(ValidationError.builder()
                    .field("page")
                    .code("PAGE_TOO_LARGE")
                    .message("Page number exceeds maximum allowed value")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        }

        // Page size validation
        if (input.pageSize() == null) {
            errors.add(ValidationError.builder()
                    .field("pageSize")
                    .code("FIELD_REQUIRED")
                    .message("Page size is required")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        } else if (input.pageSize() < 1) {
            errors.add(ValidationError.builder()
                    .field("pageSize")
                    .code("PAGE_SIZE_INVALID_RANGE")
                    .message("Page size must be greater than or equal to 1")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        } else if (input.pageSize() > 1000) {
            errors.add(ValidationError.builder()
                    .field("pageSize")
                    .code("PAGE_SIZE_TOO_LARGE")
                    .message("Page size cannot exceed 1000")
                    .severity(ErrorSeverity.ERROR)
                    .build());
        } else if (input.page() != null && input.page() > 1 && input.pageSize() < 25) {
            warnings.add("Page size less than 25 for non-first pages may not follow Open Finance recommendations");
        }

        // Pagination key validation
        if (input.paginationKey().isPresent()) {
            String key = input.paginationKey().get();
            if (key.length() > 2048) {
                errors.add(ValidationError.builder()
                        .field("paginationKey")
                        .code("PAGINATION_KEY_TOO_LONG")
                        .message("Pagination key cannot exceed 2048 characters")
                        .severity(ErrorSeverity.ERROR)
                        .build());
            }
        }
    }

    /**
     * Validates FAPI headers
     */
    private void validateFapiHeaders(GetAccountsInput input, List<ValidationError> errors, List<String> warnings) {
        // X-FAPI-Auth-Date validation (optional but format-sensitive)
        if (input.xFapiAuthDate().isPresent()) {
            String authDate = input.xFapiAuthDate().get();
            if (!FAPI_AUTH_DATE_PATTERN.matcher(authDate).matches()) {
                warnings.add("X-FAPI-Auth-Date format may not comply with RFC7231 specification");
            }
        }

        // X-FAPI-Customer-IP-Address validation (optional)
        if (input.xFapiCustomerIpAddress().isPresent()) {
            String ipAddress = input.xFapiCustomerIpAddress().get();
            if (ipAddress.length() > 100) {
                errors.add(ValidationError.builder()
                        .field("xFapiCustomerIpAddress")
                        .code("HEADER_TOO_LONG")
                        .message("X-FAPI-Customer-IP-Address cannot exceed 100 characters")
                        .severity(ErrorSeverity.ERROR)
                        .build());
            } else if (!IP_ADDRESS_PATTERN.matcher(ipAddress).matches()) {
                warnings.add("X-FAPI-Customer-IP-Address format may not be a valid IPv4 address");
            }
        }

        // X-Customer-User-Agent validation (optional)
        if (input.xCustomerUserAgent().isPresent()) {
            String userAgent = input.xCustomerUserAgent().get();
            if (userAgent.length() > 100) {
                errors.add(ValidationError.builder()
                        .field("xCustomerUserAgent")
                        .code("HEADER_TOO_LONG")
                        .message("X-Customer-User-Agent cannot exceed 100 characters")
                        .severity(ErrorSeverity.ERROR)
                        .build());
            }
        }
    }

    /**
     * Validates optional parameters and provides recommendations
     */
    private void validateOptionalParameters(GetAccountsInput input, List<String> warnings) {
        // Account type filter validation
        if (input.accountType().isEmpty()) {
            warnings.add("No account type filter specified - all account types will be returned");
        }

        // Check for optimal pagination usage
        if (input.pageSize() != null && input.pageSize() < 25 && input.page() == 1) {
            warnings.add("Small page size detected - consider using default page size of 25 for better performance");
        }
    }

    /**
     * Creates a summary of the validated input
     */
    private InputSummary createInputSummary(GetAccountsInput input) {
        return InputSummary.builder()
                .consentIdLength(input.consentId() != null ? input.consentId().length() : 0)
                .organizationIdLength(input.organizationId() != null ? input.organizationId().length() : 0)
                .hasAccountTypeFilter(input.accountType().isPresent())
                .page(input.page())
                .pageSize(input.pageSize())
                .effectivePageSize(input.getEffectivePageSize())
                .hasPaginationKey(input.hasPaginationKey())
                .hasFapiAuthDate(input.xFapiAuthDate().isPresent())
                .hasFapiCustomerIp(input.xFapiCustomerIpAddress().isPresent())
                .hasCustomerUserAgent(input.xCustomerUserAgent().isPresent())
                .build();
    }

    /**
     * Validates input and throws exception if invalid
     */
    public void validateAndThrow(GetAccountsInput input) {
        ValidationResult result = validateGetAccountsInput(input);

        if (!result.valid()) {
            String errorMessage = "Input validation failed: " +
                    result.errors().stream()
                            .map(error -> error.field() + " - " + error.message())
                            .reduce((a, b) -> a + "; " + b)
                            .orElse("Unknown validation error");

            throw new BusinessRuleViolationException(errorMessage);
        }
    }

    /**
     * Enumeration for error severity levels
     */
    public enum ErrorSeverity {
        ERROR, WARNING, INFO
    }

    /**
     * Record for validation errors
     */
    @lombok.Builder
    public record ValidationError(
            String field,
            String code,
            String message,
            ErrorSeverity severity
    ) {}

    /**
     * Record for validation results
     */
    @lombok.Builder
    public record ValidationResult(
            boolean valid,
            List<ValidationError> errors,
            List<String> warnings,
            InputSummary inputSummary
    ) {
        public boolean hasWarnings() {
            return warnings != null && !warnings.isEmpty();
        }

        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }

    /**
     * Record for input summary
     */
    @lombok.Builder
    public record InputSummary(
            int consentIdLength,
            int organizationIdLength,
            boolean hasAccountTypeFilter,
            Integer page,
            Integer pageSize,
            int effectivePageSize,
            boolean hasPaginationKey,
            boolean hasFapiAuthDate,
            boolean hasFapiCustomerIp,
            boolean hasCustomerUserAgent
    ) {}
}