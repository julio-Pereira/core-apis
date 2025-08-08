package com.openfinance.usecase.account.service;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.events.account.AccountAccessedEvent;
import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.port.IConsentValidationService;
import com.openfinance.core.port.IExternalAccountService;
import com.openfinance.core.port.IPaginationService;
import com.openfinance.core.port.IRateLimitService;
import com.openfinance.core.validation.PermissionValidationService;
import com.openfinance.usecase.IEventPublisher;
import com.openfinance.usecase.IUseCase;
import com.openfinance.usecase.pagination.PaginationLinkBuilder;
import com.openfinance.usecase.account.input.GetAccountsInput;
import com.openfinance.usecase.account.output.GetAccountsOutput;
import com.openfinance.usecase.pagination.PaginationInfo;
import com.openfinance.usecase.pagination.PaginationLinks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAccountsUseCaseImpl implements IUseCase<GetAccountsInput, GetAccountsOutput> {

    private final IConsentValidationService consentValidationService;
    private final IExternalAccountService externalAccountService;
    private final IPaginationService paginationService;
    private final IRateLimitService rateLimitService;
    private final PermissionValidationService permissionValidationService;
    private final IEventPublisher eventPublisher;
    private final PaginationLinkBuilder paginationLinkBuilder;

    @Override
    public GetAccountsOutput execute(GetAccountsInput input) {
        log.info("Executing GetAccounts use case for consentId: {}, organizationId: {}, page: {}, pageSize: {}",
                input.consentId(), input.organizationId(), input.page(), input.pageSize());

        try {
            // 1. Validate rate limits first
            validateRateLimits(input);

            // 2. Validate consent and permissions
            validateConsentAndPermissions(input);

            // 3. Validate and handle pagination key
            validatePaginationKey(input);

            // 4. Fetch accounts from external service
            List<Account> accounts = fetchAccounts(input);
            log.debug("Fetched {} accounts from external service", accounts.size());

            // 5. Calculate pagination information
            PaginationInfo paginationInfo = buildPaginationInfo(input, accounts);

            // 6. Record successful request for rate limiting
            recordSuccessfulRequest(input);

            // 7. Publish domain events
            publishAccountAccessEvent(input);

            // 8. Build and return output
            GetAccountsOutput output = GetAccountsOutput.builder()
                    .accounts(accounts)
                    .paginationInfo(paginationInfo)
                    .requestDateTime(LocalDateTime.now())
                    .build();

            log.info("Successfully executed GetAccounts use case. Returned {} accounts", accounts.size());
            return output;

        } catch (Exception e) {
            log.error("Error executing GetAccounts use case for consentId: {}, organizationId: {}",
                    input.consentId(), input.organizationId(), e);
            throw e;
        }
    }

    /**
     * Validates rate limits for the request
     */
    private void validateRateLimits(GetAccountsInput input) {
        boolean withinLimits = rateLimitService.isWithinRateLimit(
                input.organizationId(),
                "/accounts"
        );

        if (!withinLimits) {
            throw new BusinessRuleViolationException(
                    "Rate limit exceeded for organization: " + input.organizationId()
            );
        }
    }

    /**
     * Validates consent status and required permissions
     */
    private void validateConsentAndPermissions(GetAccountsInput input) {
        // Validate consent is valid and authorized
        boolean isConsentValid = consentValidationService.isConsentValid(input.consentId());
        if (!isConsentValid) {
            throw new BusinessRuleViolationException(
                    "Invalid or unauthorized consent: " + input.consentId()
            );
        }

        // Validate ACCOUNTS_READ permission
        boolean hasPermission = consentValidationService.hasPermission(
                input.consentId(),
                PermissionValidationService.ACCOUNTS_READ
        );
        permissionValidationService.validateAccountsReadPermission(hasPermission);
    }

    /**
     * Validates pagination key if present
     */
    private void validatePaginationKey(GetAccountsInput input) {
        if (input.hasPaginationKey()) {
            boolean isValidKey = paginationService.isValidPaginationKey(input.paginationKey().get());
            if (!isValidKey) {
                // Generate new pagination key for invalid/expired keys
                // This request will be counted for operational limits
                return;
            }
        }
    }

    /**
     * Fetches accounts from external service
     */
    private List<Account> fetchAccounts(GetAccountsInput input) {
        return externalAccountService.fetchAccounts(
                input.consentId(),
                input.accountType(),
                input.getZeroBasedPage(),
                input.pageSize()
        );
    }

    /**
     * Builds pagination information for the response
     */
    private PaginationInfo buildPaginationInfo(GetAccountsInput input, List<Account> accounts) {
        // Generate pagination key for this request
        String paginationKey = paginationService.generatePaginationKey(
                input.xFapiInteractionId(),
                input.page(),
                input.pageSize()
        );

        // Calculate total records and pages (simplified - in real scenario would come from service)
        int totalRecords = accounts.size(); // This should come from a count query
        int totalPages = calculateTotalPages(totalRecords, input.pageSize());

        // Build pagination links
        PaginationLinks links = paginationLinkBuilder.buildLinks(
                input,
                totalPages,
                paginationKey
        );

        return PaginationInfo.builder()
                .selfLink(links.selfLink())
                .firstLink(links.firstLink())
                .prevLink(links.prevLink())
                .nextLink(links.nextLink())
                .lastLink(links.lastLink())
                .totalRecords(totalRecords)
                .totalPages(totalPages)
                .currentPage(input.page())
                .pageSize(input.pageSize())
                .paginationKey(paginationKey)
                .build();
    }

    /**
     * Records successful request for rate limiting purposes
     */
    private void recordSuccessfulRequest(GetAccountsInput input) {
        rateLimitService.recordRequest(input.organizationId(), "/accounts");
    }

    /**
     * Publishes domain event for account access
     */
    private void publishAccountAccessEvent(GetAccountsInput input) {
        AccountAccessedEvent event = new AccountAccessedEvent(
                null, // No specific account ID for list operation
                input.consentId(),
                "GET_ACCOUNTS",
                input.organizationId()
        );

        eventPublisher.publish(event);
    }

    /**
     * Calculates total pages based on total records and page size
     */
    private int calculateTotalPages(int totalRecords, int pageSize) {
        if (totalRecords == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalRecords / pageSize);
    }
}
