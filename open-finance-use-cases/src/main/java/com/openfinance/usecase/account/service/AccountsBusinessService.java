package com.openfinance.usecase.account.service;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.enums.AccountType;
import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.port.IPaginationService;
import com.openfinance.core.validation.AccountValidationService;
import com.openfinance.usecase.account.input.GetAccountsInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business service for accounts operations
 * Contains business logic and validation rules specific to accounts use cases
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountsBusinessService {

    private final AccountValidationService accountValidationService;
    private final IPaginationService paginationService;

    /**
     * Validates input parameters for get accounts operation
     */
    public void validateGetAccountsInput(GetAccountsInput input) {
        log.debug("Validating GetAccounts input parameters");

        validatePaginationParameters(input);
        validateAccountTypeFilter(input);
        validateFapiHeaders(input);

        log.debug("Input validation completed successfully");
    }

    /**
     * Validates pagination parameters according to Open Finance standards
     */
    private void validatePaginationParameters(GetAccountsInput input) {
        // Page must be positive
        if (input.page() < 1) {
            throw new BusinessRuleViolationException("Page number must be greater than 0");
        }

        // Page size limits according to Open Finance specification
        if (input.pageSize() < 1 || input.pageSize() > 1000) {
            throw new BusinessRuleViolationException("Page size must be between 1 and 1000");
        }

        // Special rule: For non-first pages, minimum page size should be 25
        if (input.page() > 1 && input.pageSize() < 25) {
            log.warn("Page size {} is less than recommended minimum of 25 for page {}",
                    input.pageSize(), input.page());
        }
    }

    /**
     * Validates account type filter if provided
     */
    private void validateAccountTypeFilter(GetAccountsInput input) {
        if (input.accountType().isPresent()) {
            AccountType accountType = input.accountType().get();
            log.debug("Account type filter applied: {}", accountType);

            // All account types are valid for the accounts endpoint
            // Additional validation could be added here if needed
        }
    }

    /**
     * Validates FAPI (Financial-grade API) headers
     */
    private void validateFapiHeaders(GetAccountsInput input) {
        // X-FAPI-Interaction-ID is mandatory
        if (input.xFapiInteractionId() == null || input.xFapiInteractionId().trim().isEmpty()) {
            throw new BusinessRuleViolationException("X-FAPI-Interaction-ID header is mandatory");
        }

        // Validate UUID format for X-FAPI-Interaction-ID
        if (!isValidUuid(input.xFapiInteractionId())) {
            throw new BusinessRuleViolationException("X-FAPI-Interaction-ID must be a valid UUID");
        }

        log.debug("FAPI headers validation completed");
    }

    /**
     * Processes and validates the list of accounts returned from external service
     */
    public List<Account> processAccountsList(List<Account> accounts, GetAccountsInput input) {
        log.debug("Processing accounts list with {} items", accounts.size());

        if (accounts.isEmpty()) {
            log.info("No accounts found for consentId: {}", input.consentId());
            return accounts;
        }

        // Validate each account if needed
        accounts.forEach(this::validateAccountData);

        // Apply account type filter if not already filtered by external service
        List<Account> filteredAccounts = applyAccountTypeFilter(accounts, input);

        log.debug("Processed and filtered accounts list. Final count: {}", filteredAccounts.size());
        return filteredAccounts;
    }

    /**
     * Validates individual account data integrity
     */
    private void validateAccountData(Account account) {
        try {
            // Validate branch code requirements based on account type
            accountValidationService.validateBranchCodeRequirement(account);

            log.trace("Account validation passed for accountId: {}", account.getAccountId());
        } catch (Exception e) {
            log.error("Account validation failed for accountId: {}", account.getAccountId(), e);
            throw new BusinessRuleViolationException(
                    "Invalid account data for account: " + account.getAccountId(), e
            );
        }
    }

    /**
     * Applies account type filter if specified and not already filtered
     */
    private List<Account> applyAccountTypeFilter(List<Account> accounts, GetAccountsInput input) {
        if (input.accountType().isEmpty()) {
            return accounts;
        }

        AccountType filterType = input.accountType().get();
        log.debug("Applying account type filter: {}", filterType);

        List<Account> filtered = accounts.stream()
                .filter(account -> account.getType() == filterType)
                .toList();

        log.debug("Account type filter applied. Original: {}, Filtered: {}",
                accounts.size(), filtered.size());
        return filtered;
    }

    /**
     * Validates if a string is a valid UUID format
     */
    private boolean isValidUuid(String uuid) {
        if (uuid == null) {
            return false;
        }

        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }

    /**
     * Validates operational limits compliance
     */
    public void validateOperationalLimits(GetAccountsInput input) {
        log.debug("Validating operational limits for organizationId: {}", input.organizationId());

        // Check if pagination key is being used correctly for operational limits
        if (input.hasPaginationKey()) {
            boolean isValidKey = paginationService.isValidPaginationKey(input.paginationKey().get());
            if (!isValidKey) {
                log.warn("Invalid pagination key provided - this request will count towards operational limits");
            }
        }

        // Additional operational limits validation could be added here
        log.debug("Operational limits validation completed");
    }

    /**
     * Calculates effective page size according to Open Finance rules
     */
    public int calculateEffectivePageSize(GetAccountsInput input) {
        int requestedSize = input.pageSize();
        int effectiveSize = input.getEffectivePageSize();

        if (requestedSize != effectiveSize) {
            log.debug("Page size adjusted from {} to {} according to Open Finance rules",
                    requestedSize, effectiveSize);
        }

        return effectiveSize;
    }

    /**
     * Validates if the account list should be empty based on business rules
     */
    public boolean shouldReturnEmptyList(List<Account> accounts, GetAccountsInput input) {
        // Business rule: If no accounts match the filter, return empty list
        if (input.accountType().isPresent() && accounts.isEmpty()) {
            log.info("No accounts found matching filter criteria for consentId: {}, accountType: {}",
                    input.consentId(), input.accountType().get());
            return true;
        }

        return false;
    }
}