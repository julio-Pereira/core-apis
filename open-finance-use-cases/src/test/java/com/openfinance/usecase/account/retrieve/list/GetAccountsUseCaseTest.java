package com.openfinance.usecase.account.retrieve.list;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.domain.account.AccountId;
import com.openfinance.core.domain.account.AccountType;
import com.openfinance.core.domain.valueobjects.BranchCode;
import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.exceptions.ValidationException;
import com.openfinance.usecase.IEventPublisher;
import com.openfinance.usecase.account.mapper.IAccountUseCaseMapper;
import com.openfinance.usecase.account.port.IAccountPort;
import com.openfinance.usecase.account.service.AccountValidationService;
import com.openfinance.usecase.account.service.ConsentValidationService;
import com.openfinance.usecase.account.service.RateLimitValidationService;
import com.openfinance.usecase.pagination.PaginationInfo;
import com.openfinance.usecase.pagination.PaginationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Testes unitários para {@link GetAccountsUseCase}
 *
 * Testa todos os cenários do caso de uso de obtenção de contas,
 * incluindo validações, filtros, paginação e tratamento de erros.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAccountsUseCase Tests")
public class GetAccountsUseCaseTest {

    @Mock
    private ConsentValidationService consentValidationService;

    @Mock
    private RateLimitValidationService rateLimitValidationService;

    @Mock
    private AccountValidationService accountValidationService;

    @Mock
    private IAccountPort accountPort;

    @Mock
    private PaginationService paginationService;

    @Mock
    private IEventPublisher eventPublisher;

    @Mock
    private IAccountUseCaseMapper accountMapper;

    @InjectMocks
    private GetAccountsUseCase useCase;

    private GetAccountsInput validInput;
    private List<Account> mockAccounts;
    private List<AccountOutputDto> mockAccountOutputDtos;
    private PaginationInfo mockPaginationInfo;

    @BeforeEach
    void setUp() {
        validInput = GetAccountsInput.builder()
                .consentId("consent-123")
                .organizationId("org-123")
                .xFapiInteractionId("interaction-123")
                .page(1)
                .pageSize(25)
                .build();

        // Mock de conta para testes
        Account mockAccount = Account.builder()
                .accountId(AccountId.unique())
                .type(AccountType.CONTA_DEPOSITO_A_VISTA)
                .number("12345")
                .checkDigit("6")
                .branchCode(BranchCode.of("0001"))
                .build();

        mockAccounts = List.of(mockAccount);

        // Mock de AccountOutputDto para testes
        AccountOutputDto mockAccountOutput = new AccountOutputDto(
                "acc-123",
                "CONTA_DEPOSITO_A_VISTA",
                "INDIVIDUAL",
                "Conta Teste",
                "12345",
                "6",
                "0001",
                "0"
        );

        mockAccountOutputDtos = List.of(mockAccountOutput);

        mockPaginationInfo = PaginationInfo.builder()
                .selfLink("http://test.com/accounts?page=1")
                .totalRecords(1)
                .totalPages(1)
                .currentPage(1)
                .pageSize(25)
                .build();
    }

    @Test
    @DisplayName("Deve executar com sucesso quando todos os dados são válidos")
    void shouldExecuteSuccessfullyWhenAllDataIsValid() {
        // Given
        given(consentValidationService.validateConsentForOperation(
                anyString(), any(ConsentValidationService.AccountOperation.class)))
                .willReturn(validConsentValidationResult());

        given(rateLimitValidationService.isWithinLimits(anyString(), anyString()))
                .willReturn(true);

        given(accountPort.findAccountsByConsent(anyString(), anyString(), any()))
                .willReturn(mockAccounts);

        given(accountMapper.toAccountOutputDtoList(any()))
                .willReturn(mockAccountOutputDtos);

        given(paginationService.createPaginationInfo(any(), anyInt(), anyString()))
                .willReturn(mockPaginationInfo);

        // When
        GetAccountsOutput result = useCase.execute(validInput);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accounts()).hasSize(1);
        assertThat(result.accounts()).isEqualTo(mockAccountOutputDtos);
        assertThat(result.paginationInfo()).isEqualTo(mockPaginationInfo);
        assertThat(result.requestDateTime()).isNotNull();

        verify(consentValidationService).validateConsentForOperation(
                validInput.consentId(), ConsentValidationService.AccountOperation.GET_ACCOUNTS);
        verify(rateLimitValidationService).isWithinLimits(validInput.organizationId(), "/accounts");
        verify(accountPort).findAccountsByConsent(validInput.consentId(), validInput.organizationId(), null);
        verify(accountMapper).toAccountOutputDtoList(mockAccounts);
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando consentimento é inválido")
    void shouldThrowExceptionWhenConsentIsInvalid() {
        // Given
        given(consentValidationService.validateConsentForOperation(
                anyString(), any(ConsentValidationService.AccountOperation.class)))
                .willReturn(invalidConsentValidationResult());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(validInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("INVALID_CONSENT");

        verify(rateLimitValidationService, never()).isWithinLimits(anyString(), anyString());
        verify(accountPort, never()).findAccountsByConsent(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando rate limit é excedido")
    void shouldThrowExceptionWhenRateLimitExceeded() {
        // Given
        given(consentValidationService.validateConsentForOperation(
                anyString(), any(ConsentValidationService.AccountOperation.class)))
                .willReturn(validConsentValidationResult());

        given(rateLimitValidationService.isWithinLimits(anyString(), anyString()))
                .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(validInput))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("RATE_LIMIT_EXCEEDED");

        verify(accountPort, never()).findAccountsByConsent(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve validar pagination key quando fornecida")
    void shouldValidatePaginationKeyWhenProvided() {
        // Given
        GetAccountsInput inputWithPaginationKey = GetAccountsInput.builder()
                .consentId("consent-123")
                .organizationId("org-123")
                .xFapiInteractionId("interaction-123")
                .page(1)
                .pageSize(25)
                .paginationKey("invalid-key")
                .build();

        given(consentValidationService.validateConsentForOperation(
                anyString(), any(ConsentValidationService.AccountOperation.class)))
                .willReturn(validConsentValidationResult());

        given(rateLimitValidationService.isWithinLimits(anyString(), anyString()))
                .willReturn(true);

        given(paginationService.isValidPaginationKey(anyString()))
                .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(inputWithPaginationKey))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("INVALID_PAGINATION_KEY");

        verify(paginationService).isValidPaginationKey("invalid-key");
        verify(accountPort, never()).findAccountsByConsent(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve validar tipo de conta quando fornecido")
    void shouldValidateAccountTypeWhenProvided() {
        // Given
        GetAccountsInput inputWithAccountType = GetAccountsInput.builder()
                .consentId("consent-123")
                .organizationId("org-123")
                .xFapiInteractionId("interaction-123")
                .page(1)
                .pageSize(25)
                .type(AccountType.CONTA_DEPOSITO_A_VISTA)
                .build();

        given(consentValidationService.validateConsentForOperation(
                anyString(), any(ConsentValidationService.AccountOperation.class)))
                .willReturn(validConsentValidationResult());

        given(rateLimitValidationService.isWithinLimits(anyString(), anyString()))
                .willReturn(true);

        given(accountPort.findAccountsByConsent(anyString(), anyString(), any()))
                .willReturn(mockAccounts);

        given(paginationService.createPaginationInfo(any(), anyInt(), anyString()))
                .willReturn(mockPaginationInfo);

        // When
        GetAccountsOutput result = useCase.execute(inputWithAccountType);

        // Then
        assertThat(result).isNotNull();
        verify(accountValidationService).validateAccountType(AccountType.CONTA_DEPOSITO_A_VISTA);
        verify(accountPort).findAccountsByConsent(
                inputWithAccountType.consentId(),
                inputWithAccountType.organizationId(),
                AccountType.CONTA_DEPOSITO_A_VISTA);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhuma conta é encontrada")
    void shouldReturnEmptyListWhenNoAccountsFound() {
        // Given
        given(consentValidationService.validateConsentForOperation(
                anyString(), any(ConsentValidationService.AccountOperation.class)))
                .willReturn(validConsentValidationResult());

        given(rateLimitValidationService.isWithinLimits(anyString(), anyString()))
                .willReturn(true);

        given(accountPort.findAccountsByConsent(anyString(), anyString(), any()))
                .willReturn(List.of());

        given(accountMapper.toAccountOutputDtoList(any()))
                .willReturn(List.of());

        PaginationInfo emptyPaginationInfo = PaginationInfo.builder()
                .selfLink("http://test.com/accounts?page=1")
                .totalRecords(0)
                .totalPages(1)
                .currentPage(1)
                .pageSize(25)
                .build();

        given(paginationService.createPaginationInfo(any(), anyInt(), anyString()))
                .willReturn(emptyPaginationInfo);

        // When
        GetAccountsOutput result = useCase.execute(validInput);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accounts()).isEmpty();
        assertThat(result.paginationInfo().totalRecords()).isZero();

        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Deve registrar acesso ao consentimento para auditoria")
    void shouldRecordConsentAccessForAudit() {
        // Given
        given(consentValidationService.validateConsentForOperation(
                anyString(), any(ConsentValidationService.AccountOperation.class)))
                .willReturn(validConsentValidationResult());

        given(rateLimitValidationService.isWithinLimits(anyString(), anyString()))
                .willReturn(true);

        given(accountPort.findAccountsByConsent(anyString(), anyString(), any()))
                .willReturn(mockAccounts);

        given(paginationService.createPaginationInfo(any(), anyInt(), anyString()))
                .willReturn(mockPaginationInfo);

        // When
        useCase.execute(validInput);

        // Then
        verify(consentValidationService).recordConsentAccess(
                validInput.consentId(),
                "GET_ACCOUNTS",
                true);
    }

    @Test
    @DisplayName("Deve publicar evento de acesso às contas")
    void shouldPublishAccountAccessEvent() {
        // Given
        given(consentValidationService.validateConsentForOperation(
                anyString(), any(ConsentValidationService.AccountOperation.class)))
                .willReturn(validConsentValidationResult());

        given(rateLimitValidationService.isWithinLimits(anyString(), anyString()))
                .willReturn(true);

        given(accountPort.findAccountsByConsent(anyString(), anyString(), any()))
                .willReturn(mockAccounts);

        given(paginationService.createPaginationInfo(any(), anyInt(), anyString()))
                .willReturn(mockPaginationInfo);

        // When
        useCase.execute(validInput);

        // Then
        verify(eventPublisher).publish(any());
    }

    // Métodos auxiliares para criação de mocks

    private ConsentValidationService.ConsentValidationResult validConsentValidationResult() {
        return new ConsentValidationService.ConsentValidationResult(
                true,
                "Consent is valid",
                ConsentValidationService.ConsentValidationResult.ConsentValidationStatus.VALID
        );
    }

    private ConsentValidationService.ConsentValidationResult invalidConsentValidationResult() {
        return new ConsentValidationService.ConsentValidationResult(
                false,
                "Consent is invalid",
                ConsentValidationService.ConsentValidationResult.ConsentValidationStatus.INVALID
        );
    }
}