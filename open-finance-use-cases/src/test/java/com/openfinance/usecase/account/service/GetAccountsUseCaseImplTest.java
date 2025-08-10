package com.openfinance.usecase.account.service;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.enums.AccountSubType;
import com.openfinance.core.enums.AccountType;
import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.port.IConsentValidationService;
import com.openfinance.core.port.IExternalAccountService;
import com.openfinance.core.port.IPaginationService;
import com.openfinance.core.port.IRateLimitService;
import com.openfinance.core.validation.PermissionValidationService;
import com.openfinance.core.valueobjects.AccountId;
import com.openfinance.core.valueobjects.BranchCode;
import com.openfinance.core.valueobjects.CompeCode;
import com.openfinance.usecase.account.input.GetAccountsInput;
import com.openfinance.usecase.account.output.GetAccountsOutput;
import com.openfinance.usecase.pagination.PaginationLinkBuilder;
import com.openfinance.usecase.pagination.PaginationLinks;
import com.openfinance.usecase.IEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAccountsUseCaseImpl Tests")
class GetAccountsUseCaseImplTest {

    @Mock
    private IConsentValidationService consentValidationService;

    @Mock
    private IExternalAccountService externalAccountService;

    @Mock
    private IPaginationService paginationService;

    @Mock
    private IRateLimitService rateLimitService;

    @Mock
    private PermissionValidationService permissionValidationService;

    @Mock
    private IEventPublisher eventPublisher;

    @Mock
    private PaginationLinkBuilder paginationLinkBuilder;

    private GetAccountsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAccountsUseCaseImpl(
                consentValidationService,
                externalAccountService,
                paginationService,
                rateLimitService,
                permissionValidationService,
                eventPublisher,
                paginationLinkBuilder
        );
    }

    @Test
    @DisplayName("Given valid input and successful dependencies when execute then return accounts successfully")
    void givenValidInputAndSuccessfulDependencies_whenExecute_thenReturnAccountsSuccessfully() {
        // GIVEN
        GetAccountsInput validInput = createValidInputBuilder().build();
        List<Account> mockAccounts = createMockAccounts();
        PaginationLinks mockLinks = createMockPaginationLinks();

        setupSuccessfulMocks(mockAccounts, mockLinks);

        // WHEN
        GetAccountsOutput result = useCase.execute(validInput);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.accounts()).hasSize(2);
        assertThat(result.hasAccounts()).isTrue();
        assertThat(result.requestDateTime()).isNotNull();
        assertThat(result.paginationInfo()).isNotNull();

        // Verify all necessary validations were called
        verify(rateLimitService).isWithinRateLimit("org-456", "/accounts");
        verify(consentValidationService).isConsentValid("consent-123");
        verify(consentValidationService).hasPermission("consent-123", PermissionValidationService.ACCOUNTS_READ);
        verify(permissionValidationService).validateAccountsReadPermission(true);
        verify(externalAccountService).fetchAccounts(eq("consent-123"), eq(Optional.of(AccountType.CONTA_DEPOSITO_A_VISTA)), eq(0), eq(25));
        verify(rateLimitService).recordRequest("org-456", "/accounts");
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Given rate limit exceeded when execute then throw BusinessRuleViolationException")
    void givenRateLimitExceeded_whenExecute_thenThrowBusinessRuleViolationException() {
        // GIVEN
        GetAccountsInput validInput = createValidInputBuilder().build();
        when(rateLimitService.isWithinRateLimit(anyString(), anyString())).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> useCase.execute(validInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Rate limit exceeded");

        // Verify no further processing occurred
        verify(consentValidationService, never()).isConsentValid(anyString());
        verify(externalAccountService, never()).fetchAccounts(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Given invalid consent when execute then throw BusinessRuleViolationException")
    void givenInvalidConsent_whenExecute_thenThrowBusinessRuleViolationException() {
        // GIVEN
        GetAccountsInput validInput = createValidInputBuilder().build();
        when(rateLimitService.isWithinRateLimit(anyString(), anyString())).thenReturn(true);
        when(consentValidationService.isConsentValid(anyString())).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> useCase.execute(validInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Invalid or unauthorized consent");

        // Verify no further processing occurred
        verify(externalAccountService, never()).fetchAccounts(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Given permission not granted when execute then throw BusinessRuleViolationException")
    void givenPermissionNotGranted_whenExecute_thenThrowBusinessRuleViolationException() {
        // GIVEN
        GetAccountsInput validInput = createValidInputBuilder().build();
        when(rateLimitService.isWithinRateLimit(anyString(), anyString())).thenReturn(true);
        when(consentValidationService.isConsentValid(anyString())).thenReturn(true);
        when(consentValidationService.hasPermission(anyString(), anyString())).thenReturn(false);
        doThrow(new BusinessRuleViolationException("Required permission not granted"))
                .when(permissionValidationService).validateAccountsReadPermission(false);

        // WHEN & THEN
        assertThatThrownBy(() -> useCase.execute(validInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Required permission not granted");

        // Verify no further processing occurred
        verify(externalAccountService, never()).fetchAccounts(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Given empty accounts list when execute then return empty result gracefully")
    void givenEmptyAccountsList_whenExecute_thenReturnEmptyResultGracefully() {
        // GIVEN
        GetAccountsInput validInput = createValidInputBuilder().build();
        List<Account> emptyAccounts = List.of();
        PaginationLinks mockLinks = createMockPaginationLinks();

        setupSuccessfulMocks(emptyAccounts, mockLinks);

        // WHEN
        GetAccountsOutput result = useCase.execute(validInput);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.accounts()).isEmpty();
        assertThat(result.hasAccounts()).isFalse();
        assertThat(result.getAccountCount()).isZero();
        assertThat(result.paginationInfo().totalRecords()).isZero();
    }

    @Test
    @DisplayName("Given valid pagination key when execute then validate pagination key correctly")
    void givenValidPaginationKey_whenExecute_thenValidatePaginationKeyCorrectly() {
        // GIVEN
        GetAccountsInput inputWithPaginationKey = createValidInputBuilder()
                .paginationKey(Optional.of("valid-pagination-key-123"))
                .build();
        List<Account> mockAccounts = createMockAccounts();
        PaginationLinks mockLinks = createMockPaginationLinks();

        setupSuccessfulMocks(mockAccounts, mockLinks);
        when(paginationService.isValidPaginationKey("valid-pagination-key-123")).thenReturn(true);

        // WHEN
        GetAccountsOutput result = useCase.execute(inputWithPaginationKey);

        // THEN
        assertThat(result).isNotNull();
        verify(paginationService).isValidPaginationKey("valid-pagination-key-123");
    }

    @Test
    @DisplayName("Given invalid pagination key when execute then continue processing without failing")
    void givenInvalidPaginationKey_whenExecute_thenContinueProcessingWithoutFailing() {
        // GIVEN
        GetAccountsInput inputWithInvalidKey = createValidInputBuilder()
                .paginationKey(Optional.of("invalid-pagination-key"))
                .build();
        List<Account> mockAccounts = createMockAccounts();
        PaginationLinks mockLinks = createMockPaginationLinks();

        setupSuccessfulMocks(mockAccounts, mockLinks);
        when(paginationService.isValidPaginationKey("invalid-pagination-key")).thenReturn(false);

        // WHEN
        GetAccountsOutput result = useCase.execute(inputWithInvalidKey);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.hasAccounts()).isTrue();
        verify(paginationService).isValidPaginationKey("invalid-pagination-key");
    }

    @Test
    @DisplayName("Given null input when execute then throw NullPointerException")
    void givenNullInput_whenExecute_thenThrowNullPointerException() {
        // GIVEN
        GetAccountsInput nullInput = null;

        // WHEN & THEN
        assertThatThrownBy(() -> useCase.execute(nullInput))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Given external service failure when execute then throw BusinessRuleViolationException")
    void givenExternalServiceFailure_whenExecute_thenThrowBusinessRuleViolationException() {
        // GIVEN
        GetAccountsInput validInput = createValidInputBuilder().build();
        when(rateLimitService.isWithinRateLimit(anyString(), anyString())).thenReturn(true);
        when(consentValidationService.isConsentValid(anyString())).thenReturn(true);
        when(consentValidationService.hasPermission(anyString(), anyString())).thenReturn(true);
        doNothing().when(permissionValidationService).validateAccountsReadPermission(true);

        when(externalAccountService.fetchAccounts(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("External service unavailable"));

        // WHEN & THEN
        assertThatThrownBy(() -> useCase.execute(validInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Failed to fetch accounts from external service");

        // Verify request was not recorded as successful
        verify(rateLimitService, never()).recordRequest(anyString(), anyString());
    }

    // Helper methods for test setup
    private GetAccountsInput.GetAccountsInputBuilder createValidInputBuilder() {
        return GetAccountsInput.builder() // Retorna o Builder
                .consentId("consent-123")
                .organizationId("org-456")
                .accountType(Optional.of(AccountType.CONTA_DEPOSITO_A_VISTA))
                .page(1)
                .pageSize(25)
                .paginationKey(Optional.empty())
                .xFapiInteractionId(UUID.randomUUID().toString())
                .xFapiAuthDate(Optional.of("Sun, 10 Sep 2017 19:43:31 UTC"))
                .xFapiCustomerIpAddress(Optional.of("192.168.1.1"))
                .xCustomerUserAgent(Optional.of("Mozilla/5.0")); 
    }

    private List<Account> createMockAccounts() {
        Account account1 = Account.builder()
                .accountId(AccountId.of("account-1"))
                .brandName("Banco Exemplo")
                .companyCnpj("12345678000199")
                .type(AccountType.CONTA_DEPOSITO_A_VISTA)
                .subtype(AccountSubType.INDIVIDUAL)
                .compeCode(CompeCode.of("001"))
                .branchCode(BranchCode.of("1234"))
                .number("12345678")
                .checkDigit("9")
                .currency(Currency.getInstance("BRL"))
                .build();

        Account account2 = Account.builder()
                .accountId(AccountId.of("account-2"))
                .brandName("Banco Exemplo")
                .companyCnpj("12345678000199")
                .type(AccountType.CONTA_POUPANCA)
                .subtype(AccountSubType.INDIVIDUAL)
                .compeCode(CompeCode.of("001"))
                .branchCode(BranchCode.of("1234"))
                .number("87654321")
                .checkDigit("1")
                .currency(Currency.getInstance("BRL"))
                .build();

        return List.of(account1, account2);
    }

    private PaginationLinks createMockPaginationLinks() {
        return PaginationLinks.builder()
                .selfLink("https://api.banco.com.br/open-banking/accounts/v2/accounts?page=1&page-size=25")
                .firstLink(null)
                .prevLink(null)
                .nextLink("https://api.banco.com.br/open-banking/accounts/v2/accounts?page=2&page-size=25")
                .lastLink("https://api.banco.com.br/open-banking/accounts/v2/accounts?page=5&page-size=25")
                .build();
    }

    private void setupSuccessfulMocks(List<Account> accounts, PaginationLinks links) {
        // Rate limiting
        when(rateLimitService.isWithinRateLimit(anyString(), anyString())).thenReturn(true);
        doNothing().when(rateLimitService).recordRequest(anyString(), anyString());

        // Consent validation
        when(consentValidationService.isConsentValid(anyString())).thenReturn(true);
        when(consentValidationService.hasPermission(anyString(), anyString())).thenReturn(true);
        doNothing().when(permissionValidationService).validateAccountsReadPermission(true);

        // Pagination
        when(paginationService.generatePaginationKey(anyString(), anyInt(), anyInt()))
                .thenReturn("generated-pagination-key-123");

        // External service
        when(externalAccountService.fetchAccounts(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(accounts);

        // Pagination links
        when(paginationLinkBuilder.buildLinks(any(), anyInt(), anyString()))
                .thenReturn(links);

        // Event publisher
        doNothing().when(eventPublisher).publish(any());
    }
}