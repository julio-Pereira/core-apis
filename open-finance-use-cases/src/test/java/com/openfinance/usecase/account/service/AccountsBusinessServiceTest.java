package com.openfinance.usecase.account.service;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.enums.AccountSubType;
import com.openfinance.core.enums.AccountType;
import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.validation.AccountValidationService;
import com.openfinance.core.valueobjects.AccountId;
import com.openfinance.core.valueobjects.BranchCode;
import com.openfinance.core.valueobjects.CompeCode;
import com.openfinance.usecase.account.input.GetAccountsInput;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountsBusinessService Tests")
class AccountsBusinessServiceTest {

    @Mock
    private AccountValidationService accountValidationService;

    private AccountsBusinessService businessService;

    private GetAccountsInput validInput;

    @BeforeEach
    void setUp() {
        businessService = new AccountsBusinessService(accountValidationService);

        validInput = GetAccountsInput.builder()
                .consentId("consent-123")
                .organizationId("org-456")
                .accountType(Optional.of(AccountType.CONTA_DEPOSITO_A_VISTA))
                .page(1)
                .pageSize(25)
                .paginationKey(Optional.empty())
                .xFapiInteractionId(UUID.randomUUID().toString())
                .xFapiAuthDate(Optional.of("Sun, 10 Sep 2017 19:43:31 UTC"))
                .xFapiCustomerIpAddress(Optional.of("192.168.1.1"))
                .xCustomerUserAgent(Optional.of("Mozilla/5.0"))
                .build();
    }

    @Test
    @DisplayName("Should validate input successfully with valid parameters")
    void shouldValidateInputSuccessfullyWithValidParameters() {
        // Act & Assert
        assertThatCode(() -> businessService.validateGetAccountsInput(validInput))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when page is less than 1")
    void shouldThrowExceptionWhenPageIsLessThan1() {
        // Arrange
        GetAccountsInput invalidInput = validInput.builder()
                .page(0)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> businessService.validateGetAccountsInput(invalidInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Page number must be greater than 0");
    }

    @Test
    @DisplayName("Should throw exception when page size is less than 1")
    void shouldThrowExceptionWhenPageSizeIsLessThan1() {
        // Arrange
        GetAccountsInput invalidInput = validInput.builder()
                .pageSize(0)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> businessService.validateGetAccountsInput(invalidInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Page size must be between 1 and 1000");
    }

    @Test
    @DisplayName("Should throw exception when page size exceeds 1000")
    void shouldThrowExceptionWhenPageSizeExceeds1000() {
        // Arrange
        GetAccountsInput invalidInput = validInput.builder()
                .pageSize(1001)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> businessService.validateGetAccountsInput(invalidInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Page size must be between 1 and 1000");
    }

    @Test
    @DisplayName("Should throw exception when X-FAPI-Interaction-ID is null")
    void shouldThrowExceptionWhenXFapiInteractionIdIsNull() {
        // Arrange
        GetAccountsInput invalidInput = validInput.builder()
                .xFapiInteractionId(null)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> businessService.validateGetAccountsInput(invalidInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("X-FAPI-Interaction-ID header is mandatory");
    }

    @Test
    @DisplayName("Should throw exception when X-FAPI-Interaction-ID is not valid UUID")
    void shouldThrowExceptionWhenXFapiInteractionIdIsNotValidUuid() {
        // Arrange
        GetAccountsInput invalidInput = validInput.builder()
                .xFapiInteractionId("invalid-uuid-format")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> businessService.validateGetAccountsInput(invalidInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("X-FAPI-Interaction-ID must be a valid UUID");
    }

    @Test
    @DisplayName("Should process accounts list successfully")
    void shouldProcessAccountsListSuccessfully() {
        // Arrange
        List<Account> accounts = createMockAccounts();
        doNothing().when(accountValidationService).validateBranchCodeRequirement(any(Account.class));

        // Act
        List<Account> result = businessService.processAccountsList(accounts, validInput);

        // Assert
        assertThat(result).hasSize(1); // Only CONTA_DEPOSITO_A_VISTA should match filter
        assertThat(result.get(0).getType()).isEqualTo(AccountType.CONTA_DEPOSITO_A_VISTA);

        verify(accountValidationService, times(2)).validateBranchCodeRequirement(any(Account.class));
    }

    @Test
    @DisplayName("Should return empty list when no accounts match filter")
    void shouldReturnEmptyListWhenNoAccountsMatchFilter() {
        // Arrange
        GetAccountsInput inputWithFilter = GetAccountsInput.builder()
                .accountType(Optional.of(AccountType.CONTA_PAGAMENTO_PRE_PAGA))
                .build();

        List<Account> accounts = createMockAccounts(); // No pre-paid accounts in mock data
        doNothing().when(accountValidationService).validateBranchCodeRequirement(any(Account.class));

        // Act
        List<Account> result = businessService.processAccountsList(accounts, inputWithFilter);

        // Assert
        assertThat(result).isEmpty();
        assertThat(businessService.shouldReturnEmptyList(result, inputWithFilter)).isTrue();
    }

    @Test
    @DisplayName("Should handle empty accounts list gracefully")
    void shouldHandleEmptyAccountsListGracefully() {
        // Arrange
        List<Account> emptyAccounts = List.of();

        // Act
        List<Account> result = businessService.processAccountsList(emptyAccounts, validInput);

        // Assert
        assertThat(result).isEmpty();
        verify(accountValidationService, never()).validateBranchCodeRequirement(any(Account.class));
    }

    @Test
    @DisplayName("Should calculate effective page size correctly")
    void shouldCalculateEffectivePageSizeCorrectly() {
        // Test case 1: Normal page size
        GetAccountsInput input1 = validInput.builder().pageSize(50).build();
        assertThat(businessService.calculateEffectivePageSize(input1)).isEqualTo(50);

        // Test case 2: Page size less than 25 on page 1 (allowed)
        GetAccountsInput input2 = GetAccountsInput.builder().page(1).pageSize(10).build();
        assertThat(businessService.calculateEffectivePageSize(input2)).isEqualTo(10);

        // Test case 3: Page size less than 25 on page > 1 (adjusted to 25)
        GetAccountsInput input3 = GetAccountsInput.builder().page(2).pageSize(10).build();
        assertThat(businessService.calculateEffectivePageSize(input3)).isEqualTo(25);

        // Test case 4: Page size exceeds maximum (capped at 1000)
        GetAccountsInput input4 = GetAccountsInput.builder().pageSize(1500).build();
        assertThat(businessService.calculateEffectivePageSize(input4)).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should validate account data and throw exception on validation failure")
    void shouldValidateAccountDataAndThrowExceptionOnValidationFailure() {
        // Arrange
        List<Account> accounts = createMockAccounts();
        doThrow(new RuntimeException("Branch code validation failed"))
                .when(accountValidationService).validateBranchCodeRequirement(any(Account.class));

        // Act & Assert
        assertThatThrownBy(() -> businessService.processAccountsList(accounts, validInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Invalid account data for account");
    }

    @Test
    @DisplayName("Should process accounts without filter correctly")
    void shouldProcessAccountsWithoutFilterCorrectly() {
        // Arrange
        GetAccountsInput inputWithoutFilter = validInput.builder()
                .accountType(Optional.empty())
                .build();

        List<Account> accounts = createMockAccounts();
        doNothing().when(accountValidationService).validateBranchCodeRequirement(any(Account.class));

        // Act
        List<Account> result = businessService.processAccountsList(accounts, inputWithoutFilter);

        // Assert
        assertThat(result).hasSize(2); // All accounts should be returned
        verify(accountValidationService, times(2)).validateBranchCodeRequirement(any(Account.class));
    }

    private List<Account> createMockAccounts() {
        Account checkingAccount = Account.builder()
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

        Account savingsAccount = Account.builder()
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

        return List.of(checkingAccount, savingsAccount);
    }
}